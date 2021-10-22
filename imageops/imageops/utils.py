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
import subprocess
from hashlib import md5
from threading import Thread

from imageops.logger import Logger


def imageasync(function):
    """
    image async decorate to let the check and compress commonds running in backend asynchronously
    """
    def wrapper(*args, **kwargs):
        """
        wrapper
        """
        thr = Thread(target=function, args=args, kwargs=kwargs)
        thr.start()

    return wrapper


class Utils(object):
    """
    Utils Class
    """

    logger = Logger(__name__).get_logger()

    @classmethod
    @imageasync
    def get_md5_checksum(cls, image_file, check_record_file):
        """
        Get the md5 checksum of the input vm image
        """
        checksum = md5()
        with open(image_file, 'rb') as open_file:
            while True:
                data = open_file.read(4096)
                if not data:
                    break
                checksum.update(data)

        check_record = cls.read_json_file(check_record_file)
        check_record["checksum"] = checksum.hexdigest()
        cls.write_json_file(check_record_file, check_record)

        return checksum.hexdigest()

    @staticmethod
    def read_json_file(json_file):
        """
        Load the json date from file
        """
        with open(json_file, 'rb') as open_file:
            return json.load(open_file)

    @staticmethod
    def write_json_file(json_file, data):
        """
        Write the json date to the given file
        """
        with open(json_file, 'w') as open_file:
            open_file.write(json.dumps(data))

    @staticmethod
    def append_write_plain_file(plain_file, data):
        """
        Write the plain text data to the end of given file
        """
        with open(plain_file, 'a') as open_file:
            open_file.write(data)

    @classmethod
    @imageasync
    def check_cmd_exec(cls, image_file, check_record_file):
        """
        Exec the qemu-img check command to get the info of the given vm image
        """
        image_info_file = os.path.join(os.path.dirname(check_record_file), 'image_info.json')
        cmd = ['qemu-img', 'info', image_file, '--output', 'json', image_info_file]
        process = subprocess.Popen(cmd, shell=False, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)
        for line in iter(process.stdout.readline, b''):
            cls.logger.debug(line.strip().decode('unicode-escape'))
        return_code = process.wait()
        process.stdout.close()

        check_data = cls.read_json_file(check_record_file)
        if return_code != 0:
            check_data['imageInfo'] = {}
            check_data['checkResult'] = 99
            cls.write_json_file(check_record_file, check_data)
            return check_data['imageInfo']

        image_info = cls.read_json_file(image_info_file)
        if image_info.get('format') and image_info['format'] != 'qcow2':
            check_data['imageInfo'] = {'format': image_info['format']}
            check_data['checkResult'] = 5
            cls.write_json_file(check_record_file, check_data)
            return check_data['imageInfo']

        cmd = ['qemu-img', 'check', image_file, '--output', 'json']
        process = subprocess.Popen(cmd, shell=False, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)

        image_info = {}
        for line in iter(process.stdout.readline, b''):
            data = line.strip().decode('unicode-escape')
            if data in ['{', '}']:
                continue
            data = str(data).split(':')
            if len(data) != 2:
                continue
            image_info[data[0].strip('"')] = data[1].strip(',').strip().strip('"')

        return_code = process.wait()
        process.stdout.close()

        check_data = cls.read_json_file(check_record_file)
        if return_code != 0:
            check_data["imageInfo"] = {}
            check_data["checkResult"] = return_code
        else:
            check_data["imageInfo"] = image_info
            check_data["checkResult"] = return_code
        cls.write_json_file(check_record_file, check_data)

        return image_info

    @classmethod
    @imageasync
    def compress_cmd_exec(cls, input_image, output_image, compress_record_file):
        """
        Exec virt-sparsity commad to compress and convert the given vm image to qcow2
        """
        cmd = ['virt-sparsify', input_image, '--compress', '--convert', 'qcow2', output_image]
        check_tmpdir = True
        process = subprocess.Popen(cmd, shell=False, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)

        for line in iter(process.stdout.readline, b''):
            data = line.decode('unicode-escape')
            if 'Exiting because --check-tmpdir=fail was set' in data:
                check_tmpdir = False
            with open(compress_record_file, 'a') as open_file:
                open_file.write(data)

        return_code = process.wait()
        process.stdout.close()
        if return_code == 0:
            compress_output = 'Compress Completed\n'
        elif not check_tmpdir:
            compress_output = 'Compress Exiting because of No enouth space left\n'
        else:
            compress_output = 'Compress Failed\n'

        cls.append_write_plain_file(compress_record_file, compress_output)
