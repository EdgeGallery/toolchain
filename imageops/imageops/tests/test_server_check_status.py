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

import json
import os
import unittest
from unittest import mock

from imageops.server import Server
from imageops.utils import Utils


class ServerCheckStatusTest(unittest.TestCase):
    """
    Unit Test Cases about Server Module
    """

    @classmethod
    def setUpClass(cls):
        file_path = os.path.abspath(os.path.dirname(__file__))
        os.environ['HOME'] = os.path.join(file_path, 'home')
        os.environ['TMP_PATH'] = os.path.join(file_path, 'tmp')
        os.environ['IMAGE_PATH'] = os.path.join(file_path, 'vmImages')

    def setUp(self):
        self.test_server = Server('123-456-789')
        self.check_record_path = os.path.join(self.test_server.tmp_path,
                                              self.test_server.request_id)
        self.check_record_file = os.path.join(self.check_record_path,
                                              self.test_server.check_record_file)
        if not os.path.exists(self.check_record_path):
            os.makedirs(self.check_record_path)

    def tearDown(self):
        if os.path.isfile(self.check_record_file):
            os.remove(self.check_record_file)
        if os.path.exists(self.check_record_path):
            os.rmdir(self.check_record_path)
        if os.path.exists(self.test_server.tmp_path):
            os.rmdir(self.test_server.tmp_path)

    def test_get_check_status_in_progress_with_no_image_info(self):
        mock_check_info = {'checkResult': 4}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.check_rc[4], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_in_progress_with_no_checksum(self):
        mock_check_info = {'checkResult': 0, 'imageInfo': {'format': 'qcow2'}}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.check_rc[4], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_with_no_checksum(self):
        mock_check_info = {'checkResult': 4, 'imageInfo': {'format': 'qcow2'}}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.check_rc[4], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_change_filename(self):
        mock_check_info = {'checkResult': 4,
                           'imageInfo': {'filename': '/a/b/c/d.qcow2'}}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.check_rc[4], msg)
        self.assertEqual('d.qcow2', check_info.get('imageInfo').get('filename'))

    def test_get_check_status_failed(self):
        mock_check_info = {'checkResult': 99, 'imageInfo': {'format': 'qcow2'}}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(3, rc)
        self.assertEqual(self.test_server.check_rc[3], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_failed_with_checksum(self):
        mock_check_info = {'checkResult': 99, 'imageInfo': {'format': 'qcow2'}, 'checksum': '123'}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(3, rc)
        self.assertEqual(self.test_server.check_rc[3], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_timeout(self):
        mock_check_info = {'checkResult': 100, 'imageInfo': {'format': 'qcow2'},
                           'checksum': 'error'}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(6, rc)
        self.assertEqual(self.test_server.check_rc[6], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_wrong_image_format(self):
        mock_check_info = {'checkResult': 63, 'imageInfo': {'format': 'raw'}}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(5, rc)
        self.assertEqual(self.test_server.check_rc[5], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_completed(self):
        mock_check_info = {'checkResult': 0, 'imageInfo': {'format': 'qcow2'}, 'checksum': '123'}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(0, rc)
        self.assertEqual(self.test_server.check_rc[0], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_completed_with_corrupted(self):
        mock_check_info = {'checkResult': 2, 'imageInfo': {'format': 'qcow2'}, 'checksum': '123'}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(1, rc)
        self.assertEqual(self.test_server.check_rc[1], msg)
        self.assertEqual(mock_check_info, check_info)

    def test_get_check_status_completed_with_leaked(self):
        mock_check_info = {'checkResult': 3, 'imageInfo': {'format': 'qcow2'}, 'checksum': '123'}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(2, rc)
        self.assertEqual(self.test_server.check_rc[2], msg)
        self.assertEqual(mock_check_info, check_info)

    @mock.patch("imageops.utils.Utils.read_json_file")
    def test_get_check_status_io_exception(self, read_json_file):
        read_json_file.side_effect = IOError
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(3, rc)
        self.assertEqual('{}, {}'.format(self.test_server.check_rc[3], 'nonexistent request ID'), msg)
        self.assertEqual({}, check_info)

    @mock.patch("imageops.utils.Utils.read_json_file")
    def test_get_check_status_exception(self, read_json_file):
        read_json_file.side_effect = Exception
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(3, rc)
        self.assertEqual(self.test_server.check_rc[3], msg)
        self.assertEqual({}, check_info)
