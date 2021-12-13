"""
# Copyright 2021 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""

import os
import unittest
from unittest import mock

from imageops.utils import Utils, time


class UtilsTest(unittest.TestCase):
    """
    Unit Test Cases about Server Module
    """

    @classmethod
    def setUpClass(cls):
        file_path = os.path.abspath(os.path.dirname(__file__))
        os.environ['HOME'] = os.path.join(file_path, 'home')
        os.environ['TMP_PATH'] = os.path.join(file_path, 'tmp')
        os.environ['IMAGE_PATH'] = os.path.join(file_path, 'vmImages')
        cls.tmp_path = os.getenv('TMP_PATH')
        cls.request_id = '123-456-789'
        cls.configs = os.path.join(file_path, 'configs')
        cls.rate_info = {'begin': {'sig': 'Fill free space', 'base': 0, 'end': 0.3},
                         'medium':
                             {'sig': 'Copy to destination', 'base': 0.3, 'end': 0.9},
                         'end': {'sig': 'Sparsify operation', 'base': 0.9, 'end': 1.0}
                        }

    def setUp(self):
        self.compress_record_path = os.path.join(self.tmp_path, self.request_id)
        os.makedirs(self.compress_record_path)
        self.compress_record_file = os.path.join(self.compress_record_path, 'compress_status.txt')

    def tearDown(self):
        if os.path.isfile(self.compress_record_file):
            os.remove(self.compress_record_file)
        if os.path.exists(self.compress_record_path):
            os.rmdir(self.compress_record_path)
        if os.path.exists(self.tmp_path):
            os.rmdir(self.tmp_path)

    def test_internal_get_md5_checksum(self):
        image_file = os.path.join(os.getenv('IMAGE_PATH'), 'input_image_test_file.img')
        checksum = Utils._get_md5_checksum(image_file)
        self.assertEqual('05d42b3d5f9b17ae6f35411110a16aff', checksum)

    def test_get_compress_rate_with_no_compress_status_log(self):
        self.assertRaises(FileNotFoundError, Utils.get_compress_rate, self.compress_record_file)

    def test_get_compress_rate_process_zero(self):
        compress_record_file = os.path.join(self.configs, 'compress_zero.txt')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0, rate)

    def test_get_compress_rate_process_one(self):
        compress_record_file = os.path.join(self.configs, 'compress_one.txt')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0.095, rate)

    @mock.patch("imageops.utils.time.time")
    def test_get_compress_rate_process_two(self, time):
        compress_record_file = os.path.join(self.configs, 'compress_two.txt')
        time.return_value = 1638171770.3269310
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0.338, rate)

    def test_get_compress_rate_process_three(self):
        compress_record_file = os.path.join(self.configs, 'compress_three.txt')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(1.0, rate)

    def test_update_process_status_one(self):
        line = '[  22.6] Fill free space in /dev/sda1 with zero'
        process_status = {1: False, 2: False, 3: False}
        Utils.update_process_status(line, process_status, self.rate_info)
        self.assertEqual({1: True, 2: False, 3: False}, process_status)

    def test_update_process_status_two(self):
        line = '[  38.4] Copy to destination and make sparse'
        process_status = {1: True, 2: False, 3: False}
        Utils.update_process_status(line, process_status, self.rate_info)
        self.assertEqual({1: True, 2: True, 3: False}, process_status)

    def test_update_process_status_three(self):
        line = '[  92.4] Sparsify operation completed with no errors.'
        process_status = {1: True, 2: True, 3: False}
        Utils.update_process_status(line, process_status, self.rate_info)
        self.assertEqual({1: True, 2: True, 3: True}, process_status)

    def test_update_process_status_no_change(self):
        line = '136777/186400'
        process_status = {1: True, 2: False, 3: False}
        Utils.update_process_status(line, process_status, self.rate_info)
        self.assertEqual({1: True, 2: False, 3: False}, process_status)

    @mock.patch("os.statvfs")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_compress_requires_with_nonzero_rc(self, qemu_img_cmd_exec, statvfs):
        qemu_img_cmd_exec.return_value = [{}, 1]
        res = Utils.check_compress_requires('abc.img', 'a/b/c')
        self.assertEqual(False, res)

    @mock.patch("os.statvfs")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_compress_requires_without_virtual_size(self, qemu_img_cmd_exec, statvfs):
        qemu_img_cmd_exec.return_value = [{'format': 'qcow2'}, 0]
        res = Utils.check_compress_requires('abc.img', 'a/b/c')
        self.assertEqual(False, res)

    @mock.patch("os.statvfs")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_compress_requires_larger_than_free_disk(self, qemu_img_cmd_exec, statvfs):
        statvfs().f_bsize = 4096
        statvfs().f_blocks = 25737193
        statvfs().f_bfree = 13798325
        qemu_img_cmd_exec.return_value = [{'virtual-size': 64424509440}, 0]
        res = Utils.check_compress_requires('abc.img', 'a/b/c')
        self.assertEqual(False, res)

    @mock.patch("os.statvfs")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_compress_requires_smaller_than_free_rate(self, qemu_img_cmd_exec, statvfs):
        statvfs().f_bsize = 4096
        statvfs().f_blocks = 25737193
        statvfs().f_bfree = 13798325
        qemu_img_cmd_exec.return_value = [{'virtual-size': 42949672960}, 0]
        res = Utils.check_compress_requires('abc.img', 'a/b/c')
        self.assertEqual(False, res)

    @mock.patch("os.statvfs")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_compress_requires_enough_disk(self, qemu_img_cmd_exec, statvfs):
        statvfs().f_bsize = 4096
        statvfs().f_blocks = 25737193
        statvfs().f_bfree = 13798325
        qemu_img_cmd_exec.return_value = [{'virtual-size': 21474836480}, 0]
        res = Utils.check_compress_requires('abc.img', 'a/b/c')
        self.assertEqual(True, res)

    @mock.patch("imageops.utils.Utils.append_write_plain_file")
    @mock.patch("imageops.utils.Utils._virt_sparsify_cmd_exec")
    def test_compress_cmd_exec_completed(self, cmd_exec, append_write_plain_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        cmd_exec.return_value = [0, True]
        Utils.compress_cmd_exec('123.img', '456.img', '789.txt')
        Utils.logger.info.assert_called_with('Compress Completed\n')

    @mock.patch("imageops.utils.Utils.append_write_plain_file")
    @mock.patch("imageops.utils.Utils._virt_sparsify_cmd_exec")
    def test_compress_cmd_exec_no_enough_space(self, cmd_exec, append_write_plain_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        cmd_exec.return_value = [1, False]
        Utils.compress_cmd_exec('123.img', '456.img', '789.txt')
        Utils.logger.error.assert_called_with('Compress Exiting because of No enouth space left\n')

    @mock.patch("imageops.utils.Utils.append_write_plain_file")
    @mock.patch("imageops.utils.Utils._virt_sparsify_cmd_exec")
    def test_compress_cmd_exec_failed(self, cmd_exec, append_write_plain_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        cmd_exec.return_value = [1, True]
        Utils.compress_cmd_exec('123.img', '456.img', '789.txt')
        Utils.logger.error.assert_called_with('Compress Failed\n')

    @mock.patch("imageops.utils.Utils.append_write_plain_file")
    @mock.patch("imageops.utils.Utils._virt_sparsify_cmd_exec")
    def test_compress_cmd_exec_timeout(self, cmd_exec, append_write_plain_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        cmd_exec.side_effect = StopIteration
        Utils.compress_cmd_exec('123.img', '456.img', '789.txt')
        Utils.logger.exception.assert_called_with('Compress Time Out\n')

    @mock.patch("imageops.utils.Utils.append_write_plain_file")
    @mock.patch("imageops.utils.Utils._virt_sparsify_cmd_exec")
    def test_compress_cmd_exec_exception(self, cmd_exec, append_write_plain_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        cmd_exec.side_effect = Exception
        Utils.compress_cmd_exec('123.img', '456.img', '789.txt')
        Utils.logger.exception.assert_called_with('Compress Failed with exception: ')

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils._get_md5_checksum")
    def test_get_md5_checksum_success(self, get_checksum, read_json_file, write_json_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        get_checksum.return_value = 12345
        read_json_file.return_value = {'checkResult': 100}
        Utils.get_md5_checksum('123.img', '789.json')
        logger_calls = [mock.call('Successfully got checksum value: %s', 12345),
                        mock.call({'checkResult': 100, 'checksum': 12345})]
        Utils.logger.info.assert_has_calls(logger_calls)

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils._get_md5_checksum")
    def test_get_md5_checksum_timeout(self, get_checksum, read_json_file, write_json_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        get_checksum.side_effect = StopIteration
        read_json_file.return_value = {'checkResult': 1}
        Utils.get_md5_checksum('123.img', '789.json')
        Utils.logger.error.assert_called_with('Exit checksum Operation because of Time Out')
        Utils.logger.info.assert_called_with({'checkResult': 100, 'checksum': 'error'})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils._get_md5_checksum")
    def test_get_md5_checksum_exception(self, get_checksum, read_json_file, write_json_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        get_checksum.side_effect = Exception
        read_json_file.return_value = {'checkResult': 1}
        Utils.get_md5_checksum('123.img', '789.json')
        Utils.logger.error.assert_called_with('Exit CheckSum Operation because of Exception Occured')
        Utils.logger.info.assert_called_with({'checkResult': 99, 'checksum': 'error'})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils._get_md5_checksum")
    def test_get_md5_checksum_wrong_image_format(self, get_checksum, read_json_file, write_json_file):
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        get_checksum.side_effect = Exception
        read_json_file.return_value = {'checkResult': 63}
        Utils.get_md5_checksum('123.img', '789.json')
        Utils.logger.error.assert_called_with('Exit CheckSum Operation because of Exception Occured')
        Utils.logger.info.assert_called_with({'checkResult': 63, 'checksum': 'error'})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_info_exec_failed(self, qemu_img_cmd_exec, read_json_file, write_json_file):
        cmd = ['qemu-img', 'info', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        qemu_img_cmd_exec.return_value = [{'format': 'qcow2'}, 1]
        read_json_file.return_value = {'checkResult': 4}
        res = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Failed to exec cmd: %s', cmd)
        calls = [mock.call(['qemu-img', 'info', '123.img', '--output', 'json']),
                 mock.call({'checkResult': 99, 'imageInfo': {'format': 'qcow2'}})]
        Utils.logger.debug.assert_has_calls(calls)

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_info_wrong_format(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'info', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        info_mock = {'format': 'raw', 'virtual_size': 2.199, 'disk_size': '1419333632'}
        qemu_img_cmd_exec.return_value = [{'format': 'raw', 'virtual-size': '2361393152',
                                           'actual-size': '1419333632'}, 0]
        read_json_file.return_value = {'checkResult': 4}
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Does not accept image with type %s', 'raw')
        Utils.logger.debug.assert_called_with({'imageInfo': info_mock, 'checkResult': 63})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_info_timeout(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'info', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        read_json_file.return_value = {'checkResult': 4}
        qemu_img_cmd_exec.side_effect = StopIteration
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Exit cmd: %s, because of Time Out', cmd)
        Utils.logger.debug.assert_called_with({'imageInfo': {}, 'checkResult': 100})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_info_exception(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'info', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        read_json_file.return_value = {'checkResult': 4}
        qemu_img_cmd_exec.side_effect = Exception
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Exit cmd: %s, because of Exception Occured', cmd)
        Utils.logger.debug.assert_called_with({'imageInfo': {}, 'checkResult': 99})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_check_fail(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'check', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        exec_1 = [{'format': 'qcow2', 'virtual-size': '2361393152', 'actual-size': '1419333632'}, 0]
        exec_2 = [{'format': 'qcow2'}, 1]
        mock_info = {'format': 'qcow2', "virtual_size": 2.199, "disk_size": '1419333632'}
        qemu_img_cmd_exec.side_effect = [exec_1, exec_2]
        read_json_file.side_effect = [{'checkResult': 4}, {'checkResult': 4}]
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Failed to exec cmd: %s', cmd)
        Utils.logger.debug.assert_called_with({"checkResult": 1, "imageInfo": mock_info})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_check_timeout(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'check', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        exec_1 = [{'format': 'qcow2', 'virtual-size': '2361393152', 'actual-size': '1419333632'}, 0]
        exec_2 = StopIteration
        qemu_img_cmd_exec.side_effect = [exec_1, exec_2]
        read_json_file.side_effect = [{'checkResult': 4}, {'checkResult': 4}]
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Exit cmd: %s, because of Time Out', cmd)
        Utils.logger.debug.assert_called_with({"checkResult": 100, "imageInfo": {}})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_check_exception(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'check', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        exec_1 = [{'format': 'qcow2', 'virtual-size': '2361393152', 'actual-size': '1419333632'}, 0]
        exec_2 = Exception
        qemu_img_cmd_exec.side_effect = [exec_1, exec_2]
        read_json_file.side_effect = [{'checkResult': 4}, {'checkResult': 4}]
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.error.assert_called_with('Exit cmd: %s, because of Exception Occured', cmd)
        Utils.logger.debug.assert_called_with({"checkResult": 99, "imageInfo": {}})

    @mock.patch("imageops.utils.Utils.write_json_file")
    @mock.patch("imageops.utils.Utils.read_json_file")
    @mock.patch("imageops.utils.Utils.qemu_img_cmd_exec")
    def test_check_cmd_exec_success(self, qemu_img_cmd_exec, read_json_file, write_file):
        cmd = ['qemu-img', 'check', '123.img', '--output', 'json']
        logger_mock = mock.Mock()
        Utils.logger = logger_mock
        exec_1 = [{'format': 'qcow2', 'virtual-size': '2361393152', 'actual-size': '1419333632'}, 0]
        exec_2 = [{'format': 'qcow2'}, 0]
        mock_info = {'format': 'qcow2', "virtual_size": 2.199, "disk_size": '1419333632'}
        qemu_img_cmd_exec.side_effect = [exec_1, exec_2]
        read_json_file.side_effect = [{'checkResult': 4}, {'checkResult': 4}]
        image_info = Utils.check_cmd_exec('123.img', '456.json')
        Utils.logger.debug.assert_called_with({"checkResult": 0, "imageInfo": mock_info})
