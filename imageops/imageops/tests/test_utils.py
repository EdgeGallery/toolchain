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

    def setUp(self):
        self.compress_record_path = os.path.join(self.tmp_path, self.request_id)
        self.compress_record_file = os.path.join(self.compress_record_path, 'compress_status.log')

    def tearDown(self):
        if os.path.isfile(self.compress_record_file):
            os.remove(self.compress_record_file)
        if os.path.exists(self.compress_record_path):
            os.rmdir(self.compress_record_path)
        if os.path.exists(self.tmp_path):
            os.rmdir(self.tmp_path)

    def test_get_compress_rate_with_no_compress_status_log(self):
        self.assertRaises(FileNotFoundError, Utils.get_compress_rate, self.compress_record_file)

    def test_get_compress_rate_process_zero(self):
        compress_record_file = os.path.join(self.configs, 'compress_zero.log')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0, rate)

    def test_get_compress_rate_process_one(self):
        compress_record_file = os.path.join(self.configs, 'compress_one.log')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0.095, rate)

    @mock.patch("imageops.utils.time.time")
    def test_get_compress_rate_process_two(self, time):
        compress_record_file = os.path.join(self.configs, 'compress_two.log')
        time.return_value = 1638171770.3269310
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(0.338, rate)

    def test_get_compress_rate_process_three(self):
        compress_record_file = os.path.join(self.configs, 'compress_three.log')
        rate = Utils.get_compress_rate(compress_record_file)
        self.assertEqual(1.0, rate)
