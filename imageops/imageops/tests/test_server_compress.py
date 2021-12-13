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


class ServerCompressTest(unittest.TestCase):
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
        self.input_image = os.path.join(os.getenv('IMAGE_PATH'), 'input_image_test_file.img')
        self.output_image = os.path.join(os.getenv('IMAGE_PATH'), 'output_image_test_file.img')
        self.compress_record_path = os.path.join(self.test_server.tmp_path,
                                                 self.test_server.request_id)
        self.compress_record_file = os.path.join(self.compress_record_path,
                                                 self.test_server.compress_record_file)

    def tearDown(self):
        if os.path.isfile(self.compress_record_file):
            os.remove(self.compress_record_file)
        if os.path.exists(self.compress_record_path):
            os.rmdir(self.compress_record_path)
        if os.path.exists(self.test_server.tmp_path):
            os.rmdir(self.test_server.tmp_path)

    def test_compress_vm_image_with_no_input_image(self):
        self.assertRaises(ValueError, self.test_server.compress_vm_image)

    def test_compress_vm_image_with_no_out_image(self):
        self.assertRaises(ValueError, self.test_server.compress_vm_image, self.input_image)

    def test_compress_vm_image_with_nonexist_image(self):
        input_image = os.path.join(os.getenv('IMAGE_PATH'), 'abc.img')
        self.assertRaises(ValueError, self.test_server.compress_vm_image,
                          input_image, self.output_image)

    @mock.patch("imageops.utils.Utils.check_compress_requires")
    def test_compress_vm_image_no_enough_disk(self, check_compress_requires):
        check_compress_requires.return_value = False
        status, msg = self.test_server.compress_vm_image(self.input_image, self.output_image)
        self.assertEqual(1, status)
        self.assertEqual(self.test_server.compress_rc.get(3), msg)

    @mock.patch("imageops.utils.Utils.check_compress_requires")
    @mock.patch("imageops.utils.Utils.compress_cmd_exec")
    def test_compress_vm_image_with_enough_disk(self, compress_cmd_exec, check_compress_requires):
        check_compress_requires.return_value = True
        status, msg = self.test_server.compress_vm_image(self.input_image, self.output_image)
        self.assertEqual(0, status)
        self.assertEqual(self.test_server.compress_rc.get(1), msg)

    @mock.patch("imageops.utils.Utils.check_compress_requires")
    @mock.patch("imageops.utils.Utils.compress_cmd_exec")
    def test_compress_vm_image_with_exception(self, compress_cmd_exec, check_compress_requires):
        check_compress_requires.side_effect = Exception
        status, msg = self.test_server.compress_vm_image(self.input_image, self.output_image)
        self.assertEqual(1, status)
        self.assertEqual(self.test_server.compress_rc.get(2), msg)
