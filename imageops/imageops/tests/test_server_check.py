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
from pathlib import Path

from imageops.server import Server


class ServerTest(unittest.TestCase):
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

    def tearDown(self):
        if os.path.isfile(self.check_record_file):
            os.remove(self.check_record_file)
        os.rmdir(self.check_record_path)
        os.rmdir(self.test_server.tmp_path)

    def test_check_vm_image_without_exception(self):
        mock_check_info = {'checkResult': 4}
        with open(self.check_record_file, 'w') as open_file:
             open_file.write(json.dumps(mock_check_info))
        rc, msg, check_info = self.test_server.get_check_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.check_rc[4], msg)
        self.assertEqual(mock_check_info, check_info)

