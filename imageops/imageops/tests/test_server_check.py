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

from imageops.server import Server
from imageops.utils import Utils


class ServerCheckTest(unittest.TestCase):
    """
    Unit Test Cases about Server Module
    """

    def setUp(self):
        file_path = os.path.abspath(os.path.dirname(__file__))
        os.environ['HOME'] = os.path.join(file_path, 'home')
        os.environ['TMP_PATH'] = os.path.join(file_path, 'tmp')
        os.environ['IMAGE_PATH'] = os.path.join(file_path, 'vmImages')
        self.test_server = Server('123-456-789')
        self.input_image = os.path.join(os.getenv('IMAGE_PATH'), 'input_image_test_file.img')
        self.check_record_path = os.path.join(self.test_server.tmp_path,
                                              self.test_server.request_id)
        self.check_record_file = os.path.join(self.check_record_path,
                                              self.test_server.check_record_file)

    def tearDown(self):
        if os.path.isfile(self.check_record_file):
            os.remove(self.check_record_file)
        if os.path.exists(self.check_record_path):
            os.rmdir(self.check_record_path)
        if os.path.exists(self.test_server.tmp_path):
            os.rmdir(self.test_server.tmp_path)

    def test_server_init_without_request_id(self):
        logger_mock = mock.Mock()
        Server.logger = logger_mock
        self.assertRaises(ValueError, Server)
        Server.logger.error.assert_called_with('Lacking request_id.')

    def test_server_init_without_tmp_env(self):
        logger_mock = mock.Mock()
        Server.logger = logger_mock
        os.environ.pop('TMP_PATH')
        self.assertRaises(ValueError, Server, 'abc-xyz')
        Server.logger.error.assert_called_with('No TMP_PATH found in env.')

    def test_server_init_without_image_env(self):
        logger_mock = mock.Mock()
        Server.logger = logger_mock
        os.environ.pop('IMAGE_PATH')
        self.assertRaises(ValueError, Server, 'abc-xyz')
        Server.logger.error.assert_called_with('No IMAGE_PATH found in env.')

    @mock.patch("imageops.utils.Utils.check_cmd_exec")
    @mock.patch("imageops.utils.Utils.get_md5_checksum")
    def test_check_vm_image_without_exception(self, get_md5_checksum, check_cmd_exec):
        check_cmd_exec.return_value = {"format": "qcow2", "virtual_size": 40.0}
        get_md5_checksum.return_value = '123'
        status, msg = self.test_server.check_vm_image(self.input_image)
        self.assertEqual(0, status)
        self.assertEqual('Check In Progress', msg)

    def test_check_vm_image_with_no_input_image(self):
        self.assertRaises(ValueError, self.test_server.check_vm_image)

    def test_check_vm_image_with_nonexist_input_image(self):
        self.assertRaises(ValueError, self.test_server.check_vm_image, 'nonexosts.img')

    @mock.patch("imageops.utils.Utils.check_cmd_exec")
    @mock.patch("imageops.utils.Utils.get_md5_checksum")
    def test_check_vm_image_failed(self, get_md5_checksum, check_cmd_exec):
        check_cmd_exec.side_effect = Exception
        get_md5_checksum.return_value = '123'
        status, msg = self.test_server.check_vm_image(self.input_image)
        self.assertEqual(1, status)
        self.assertEqual('Check Failed', msg)
