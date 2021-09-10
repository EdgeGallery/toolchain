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
from pathlib import Path

from imageops.utils import Utils

class Server(object):

    def __init__(self, request_id=None):
        if not request_id:
            raise Exception('Lacking request_id\n')
        self.request_id = str(request_id)

        if os.getenv('TMP_PATH'):
            self.tmp_path = os.getenv('TMP_PATH')
        else:
            raise Exception('No TMP_PATH found in env\n')

        if os.getenv('IMAGE_PATH'):
            self.image_path = os.getenv('IMAGE_PATH')
        else:
            raise Exception('No IMAGE_PATH found in env.\n')

        self.check_record_file = 'check_info.json'
        self.compress_record_file = 'compress_status.json'

    def check_vm_image(self, input_image=None):
        if not input_image:
            raise Exception('No image is given\n')

        image = Path(input_image)
        if not image.is_file():
            raise Exception('Given image {} is not exist\n'.format(input_image))

        try:
            check_record_path = os.path.join(self.tmp_path, self.request_id)
            os.makedirs(check_record_path)
            check_record_file = os.path.join(check_record_path, self.check_record_file)

            check_info = {}
            check_info['imageInfo'] = Utils.check_cmd_exec(input_image, check_record_file)
            check_info['checksum'] = Utils.get_md5_checksum(input_image, check_record_file)

            Utils.write_json_file(check_record_file, check_info)

            status = 0
            msg = "Check In Progress"
        except Exception as e:
            status = 1
            msg = str(e)

        return status, msg

    def get_check_status(self):
        check_record_file = os.path.join(self.tmp_path, self.request_id, self.check_record_file)
        check_info = Utils.read_json_file(check_record_file)
        if check_info.get('checksum'):
            if check_info.get('checkResult') == 0:
                return 0, "Check Completed, the image is (now) consistent", check_info
            if check_info.get('checkResult') == 2:
                return 1, "Check completed, image is corrupted", check_info
            if check_info.get('checkResult') == 3:
                return 2, 'Check completed, image has leaked clusters, but is not corrupted', check_info
            return 3, 'Check failed', check_info

        return 4, "Check in Progress", check_info

    def compressVMImage(self, inputImage=None, outputImage=None):
        if not inputImage:
            raise Exception('No image is given\n')
        if not outputImage:
            raise Exception('No output image path is given\n')

        image = Path(inputImage)
        if not image.is_file():
            raise Exception('Given image {} is not exist\n'.format(inputImage))

        status = 0
        msg = "Compress In Progress"
        return status, msg

    def getCompressStatus(self):
        status = 0
        msg = "Compress Completed"
        rate = 1
        return status, msg, rate
