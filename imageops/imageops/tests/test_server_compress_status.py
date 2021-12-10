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
        os.environ['TMP_PATH'] = file_path
        os.environ['IMAGE_PATH'] = os.path.join(file_path, 'vmImages')

    def setUp(self):
        self.test_server = Server('configs')
        self.compress_record_path = os.path.join(self.test_server.tmp_path,
                                                 self.test_server.request_id)
        if not os.path.exists(self.compress_record_path):
            os.makedirs(self.compress_record_path)

    def test_get_compress_status_with_nonexist_compress_file(self):
        self.test_server.request_id = 'abc-def-xyz'
        rc, msg, rate = self.test_server.get_compress_status()
        mock_msg = '{}, {}'.format(self.test_server.compress_rc[2], 'nonexistent request ID')
        self.assertEqual(2, rc)
        self.assertEqual(mock_msg, msg)
        self.assertEqual(0, rate)

    def test_get_compress_status_completed(self):
        self.test_server.compress_record_file = 'compress_completed.txt'
        rc, msg, rate = self.test_server.get_compress_status()
        self.assertEqual(0, rc)
        self.assertEqual(self.test_server.compress_rc[0], msg)
        self.assertEqual(1, rate)

    def test_get_compress_status_no_enough_disk(self):
        self.test_server.compress_record_file = 'compress_no_enough_disk.txt'
        rc, msg, rate = self.test_server.get_compress_status()
        self.assertEqual(3, rc)
        self.assertEqual(self.test_server.compress_rc[3], msg)
        self.assertEqual(0, rate)

    def test_get_compress_status_timeout(self):
        self.test_server.compress_record_file = 'compress_timeout.txt'
        rc, msg, rate = self.test_server.get_compress_status()
        self.assertEqual(4, rc)
        self.assertEqual(self.test_server.compress_rc[4], msg)
        self.assertEqual(0, rate)

    @mock.patch("imageops.utils.Utils.get_compress_rate")
    def test_get_compress_status_in_progress(self, get_compress_rate):
        self.test_server.compress_record_file = 'compress_one.txt'
        get_compress_rate.return_value = 0.123
        rc, msg, rate = self.test_server.get_compress_status()
        self.assertEqual(1, rc)
        self.assertEqual(self.test_server.compress_rc[1], msg)
        self.assertEqual(0.123, rate)
