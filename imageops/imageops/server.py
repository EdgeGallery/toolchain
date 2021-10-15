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
            raise ValueError('Lacking request_id\n')
        self.request_id = str(request_id)

        if os.getenv('TMP_PATH'):
            self.tmp_path = os.getenv('TMP_PATH')
        else:
            raise ValueError('No TMP_PATH found in env\n')

        if os.getenv('IMAGE_PATH'):
            self.image_path = os.getenv('IMAGE_PATH')
        else:
            raise ValueError('No IMAGE_PATH found in env.\n')

        self.check_record_file = 'check_info.json'
        self.compress_record_file = 'compress_status.log'
        self.compress_status_complete = 'Compress Completed'
        self.compress_status_in_progress = 'Compress In Progress'
        self.compress_status_failed = 'Compress Failed'

    def check_vm_image(self, input_image=None):
        if not input_image:
            raise ValueError('No image is given\n')

        image = Path(input_image)
        if not image.is_file():
            raise ValueError('Given image {} is not exist\n'.format(input_image))

        try:
            check_record_path = os.path.join(self.tmp_path, self.request_id)
            os.makedirs(check_record_path)
            check_record_file = os.path.join(check_record_path, self.check_record_file)

            check_info = {}
            check_info['imageInfo'] = Utils.check_cmd_exec(input_image, check_record_file)
            check_info['checksum'] = Utils.get_md5_checksum(input_image, check_record_file)

            Utils.write_json_file(check_record_file, check_info)

            status = 0
            msg = 'Check In Progress'
        except Exception:
            status = 1
            msg = 'Check Failed'
            check_info = {}
            check_info['checkResult'] = 99
            Utils.write_json_file(check_record_file, check_info)

        return status, msg

    def get_check_status(self):
        check_record_file = os.path.join(self.tmp_path, self.request_id, self.check_record_file)
        check_info = Utils.read_json_file(check_record_file)

        if check_info.get('imageInfo'):
            image_info = check_info.get('imageInfo')
            if image_info.get('filename'):
                file_name = image_info.get('filename').split('/')[-1]
                check_info['imageInfo']['filename'] = file_name
        if check_info.get('checksum'):
            if check_info.get('checkResult') == 0:
                return 0, "Check Completed, the image is (now) consistent", check_info
            if check_info.get('checkResult') == 2:
                return 1, "Check completed, image is corrupted", check_info
            if check_info.get('checkResult') == 3:
                return 2, 'Check completed, image has leaked clusters, but is not corrupted', check_info
            return 3, 'Check failed', check_info

        if check_info.get('checkResult') == 99:
            return 3, 'Check failed', check_info
        return 4, "Check in Progress", check_info

    def compress_vm_image(self, input_image=None, output_image=None):
        if not input_image:
            raise ValueError('No image is given\n')
        if not output_image:
            raise ValueError('No output image path is given\n')

        image = Path(input_image)
        if not image.is_file():
            raise ValueError('Image {} is not exist\n'.format(input_image))

        try:
            compress_record_path = os.path.join(self.tmp_path, self.request_id)
            os.makedirs(compress_record_path)
            compress_record_file = os.path.join(compress_record_path, self.compress_record_file)

            data = 'Start to compress...\n'
            Utils.append_write_plain_file(compress_record_file, data)
            Utils.compress_cmd_exec(input_image, output_image, compress_record_file)

            status = 0
            msg = '{}\n'.format(self.compress_status_in_progress)
        except Exception:
            status = 1
            msg = '{}\n'.format(self.compress_status_failed)
            Utils.append_write_plain_file(compress_record_file, msg)

        return status, msg

    def get_compress_status(self):
        try:
            compress_record_file = os.path.join(self.tmp_path,
                                                self.request_id,
                                                self.compress_record_file)
            with open(compress_record_file, 'r') as f:
                for line in f:
                    if self.compress_status_complete in line:
                        return 0, self.compress_status_complete, 1
                    if self.compress_status_failed in line:
                        return 2, self.compress_status_failed, 0
            return 1, self.compress_status_in_progress, 0.5
        except Exception:
            return 2, self.compress_status_failed, 0
