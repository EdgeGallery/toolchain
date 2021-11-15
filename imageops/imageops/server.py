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
from pathlib import Path

from imageops.utils import Utils
from imageops.logger import Logger

class Server(object):
    """
    Backend server for imageops API
    The request_id is the only input param which used to identify this request
    """

    logger = Logger(__name__).get_logger()

    def __init__(self, request_id=None):
        """
        Init Server class
        """
        if not request_id:
            msg = 'Lacking request_id.'
            self.logger.error(msg)
            raise ValueError(msg)
        self.request_id = str(request_id)

        if not os.getenv('TMP_PATH'):
            msg = 'No TMP_PATH found in env.'
            self.logger.error(msg)
            raise ValueError(msg)
        self.tmp_path = os.getenv('TMP_PATH')

        if not os.getenv('IMAGE_PATH'):
            msg = 'No IMAGE_PATH found in env.'
            self.logger.error(msg)
            raise ValueError(msg)
        self.image_path = os.getenv('IMAGE_PATH')

        self.check_record_file = 'check_info.json'
        self.compress_record_file = 'compress_status.log'

        self.check_rc = {0: 'Check Completed, the image is (now) consistent',
                         1: 'Check completed, image is corrupted',
                         2: 'Check completed, image has leaked clusters, but is not corrupted',
                         3: 'Check failed',
                         4: 'Check in Progress',
                         5: 'Check Exiting because of not support this type of image',
                         6: 'Check Time Out'}
        self.compress_rc = {0: 'Compress Completed',
                            1: 'Compress In Progress',
                            2: 'Compress Failed',
                            3: 'Compress Exiting because of No enouth space left',
                            4: 'Compress Time Out'}

    def check_vm_image(self, input_image=None):
        """
        Check the input vm image to get it's checksum and basic info such as type and size
        """
        self.logger.info('Start to check VM image {} ...'.format(input_image))
        if not input_image:
            msg = 'No image is given to do the check.'
            self.logger.error(msg)
            raise ValueError(msg)

        image = Path(input_image)
        if not image.is_file():
            msg = 'Given image {} is not exist.'.format(input_image)
            self.logger.error(msg)
            raise ValueError(msg)

        try:
            check_record_path = os.path.join(self.tmp_path, self.request_id)
            os.makedirs(check_record_path)
            check_record_file = os.path.join(check_record_path, self.check_record_file)

            check_info = {'checkResult': 4}
            check_info['imageInfo'] = Utils.check_cmd_exec(input_image, check_record_file)
            check_info['checksum'] = Utils.get_md5_checksum(input_image, check_record_file)

            Utils.write_json_file(check_record_file, check_info)

            status = 0
            msg = 'Check In Progress'
        except Exception as exception:
            status = 1
            msg = 'Check Failed'
            check_info = {'checkResult': 99}
            Utils.write_json_file(check_record_file, check_info)
            self.logger.error(exception)

        self.logger.info('Check image {}, status: {}, msg: {}'.format(input_image, status, msg))
        return status, msg

    def get_check_status(self):
        """
        Get the status of one check with the request ID
        """
        self.logger.info('Start to get check status...')
        check_record_file = os.path.join(self.tmp_path, self.request_id, self.check_record_file)
        check_info = Utils.read_json_file(check_record_file)

        self.logger.debug(check_info)

        if check_info.get('imageInfo'):
            image_info = check_info.get('imageInfo')
            if image_info.get('filename'):
                file_name = image_info.get('filename').split('/')[-1]
                check_info['imageInfo']['filename'] = file_name

        if check_info.get('checkResult') == 99:
            return 3, self.check_rc[3], check_info
        if check_info.get('checkResult') == 100:
            return 6, self.check_rc[6], check_info

        if not check_info.get('imageInfo'):
            return 4, self.check_rc[4], check_info
        if not check_info.get('checksum'):
            if check_info.get('checkResult') == 63:
                return 5, self.check_rc[5], check_info
            return 4, self.check_rc[4], check_info
        if check_info.get('checkResult') == 0:
            return 0, self.check_rc[0], check_info
        if check_info.get('checkResult') == 2:
            return 1, self.check_rc[1], check_info
        if check_info.get('checkResult') == 3:
            return 2, self.check_rc[2], check_info
        if check_info.get('checkResult') == 4:
            return 4, self.check_rc[4], check_info
        if check_info.get('checkResult') == 63:
            return 5, self.check_rc[5], check_info
        return 3, self.check_rc[3], check_info

    def compress_vm_image(self, input_image=None, output_image=None):
        """
        Compress the input vm image to get a slim one which is sparsify
        Also can transfer raw image to qcow2 one
        """
        self.logger.info('Start to compress vm image {} ...'.format(input_image))
        if not input_image:
            msg = 'No image is given.'
            self.logger.error(msg)
            raise ValueError(msg)
        if not output_image:
            msg = 'No output image path is given.'
            self.logger.error(msg)
            raise ValueError(msg)

        image = Path(input_image)
        if not image.is_file():
            msg = 'Image {} is not exist.'.format(input_image)
            self.logger.error(msg)
            raise ValueError(msg)

        try:
            compress_record_path = os.path.join(self.tmp_path, self.request_id)
            os.makedirs(compress_record_path)
            compress_record_file = os.path.join(compress_record_path, self.compress_record_file)

            self.logger.info('Start to compress image {} ...'.format(input_image))
            Utils.compress_cmd_exec(input_image, output_image, compress_record_file)

            status = 0
            msg = '{}'.format('Compress In Progress')
        except Exception as exception:
            self.logger.error(exception)
            status = 1
            msg = '{}'.format('Compress Failed')
            Utils.append_write_plain_file(compress_record_file, msg)

        self.logger.info('Compress image {} with status: {} and msg: {}'
                         .format(input_image, status, msg))
        return status, msg

    def get_compress_status(self):
        """
        Get the status of one compress with the request ID
        """
        self.logger.info('Start to get status of compress image ...')
        try:
            compress_record_file = os.path.join(self.tmp_path,
                                                self.request_id,
                                                self.compress_record_file)
            with open(compress_record_file, 'r') as compress_file:
                for line in compress_file:
                    if self.compress_rc[0] in line:
                        return 0, self.compress_rc[0], 1
                    if self.compress_rc[2] in line:
                        return 2, self.compress_rc[2], 0
                    if self.compress_rc[3] in line:
                        return 3, self.compress_rc[3], 0
                    if self.compress_rc[4] in line:
                        return 4, self.compress_rc[4], 0
            return 1, self.compress_rc[1], 0.5
        except Exception as exception:
            self.logger.error(exception)
            return 2, self.compress_rc[2], 0
