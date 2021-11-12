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
import time
from hashlib import md5
from threading import Thread
import timeout_decorator

from imageops.logger import Logger


TIMEOUT = float(os.getenv('IMAGEOPS_TIMEOUT'))

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
    @timeout_decorator.timeout(TIMEOUT, timeout_exception=StopIteration, use_signals=False)
    def _get_md5_checksum(cls, image_file):
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
        return checksum.hexdigest()

    @classmethod
    @imageasync
    def get_md5_checksum(cls, image_file, check_record_file):
        try:
            checksum = cls._get_md5_checksum(image_file)
            check_result = 4
            cls.logger.info('Successfully got checksum value: {}'.format(checksum))
        except StopIteration:
            cls.logger.error('Exit checksum Operation because of Time Out')
            check_result = 100
            checksum = 'error'
        except Exception as exception:
            cls.logger.error('Exit CheckSum Operation because of Exception Occured')
            cls.logger.error(exception)
            check_result = 99
            checksum = 'error'
        finally:
            check_record = cls.read_json_file(check_record_file)
            if check_record.get('checkResult'):
                if check_result == 100:
                    check_record['checkResult'] = 100
                if check_result == 99 and check_record['checkResult'] != 63:
                    check_record['checkResult'] = 99
            else:
                check_record['checkResult'] = check_result

            check_record['checksum'] = checksum
            cls.write_json_file(check_record_file, check_record)
            cls.logger.info(check_record)
            return checksum

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
    @timeout_decorator.timeout(TIMEOUT, timeout_exception=StopIteration, use_signals=False)
    def qemu_img_cmd_exec(cls, cmd):
        process = subprocess.Popen(cmd, shell=False, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)
        image_info = {}
        for line in iter(process.stdout.readline, b''):
            data = line.strip().decode('unicode-escape')
            cls.logger.debug(data)
            if data in ['{', '}']:
                continue
            data = str(data).split(':')
            if len(data) != 2:
                continue
            image_info[data[0].strip('"')] = data[1].strip(',').strip().strip('"')

        return_code = process.wait()
        process.stdout.close()
        return image_info, return_code

    @classmethod
    @imageasync
    def check_cmd_exec(cls, image_file, check_record_file):
        """
        Exec the qemu-img check command to get the info of the given vm image
        """
        cmd = ['qemu-img', 'info', image_file, '--output', 'json']
        cls.logger.debug(cmd)
        try:
            image_info, return_code = cls.qemu_img_cmd_exec(cmd)
            if return_code == 0:
                check_result = 4
                cls.logger.info('Successfully exec cmd: {}'.format(cmd))

            if return_code != 0:
                cls.logger.error('Failed to exec cmd: {}'.format(cmd))
                image_info = {}
                check_result = 99
                return image_info

            if image_info.get('format') and image_info['format'] != 'qcow2':
                cls.logger.error('Does not accept image with type {}'.format(image_info['format']))
                image_info = {'format': image_info['format']}
                check_result = 63
                return image_info
        except StopIteration:
            cls.logger.error('Exit cmd: {}, because of Time Out'.format(cmd))
            image_info = {}
            check_result = 100
            return image_info
        except Exception as exception:
            cls.logger.error('Exit cmd: {}, because of Exception Occured'.format(cmd))
            image_info = {}
            check_result = 99
            return image_info
        finally:
            check_data = cls.read_json_file(check_record_file)
            check_data['imageInfo'] = image_info
            if not check_data.get('checkResult') or not check_data['checkResult'] in [99, 100]:
                check_data['checkResult'] = check_result
            cls.write_json_file(check_record_file, check_data)
            cls.logger.debug(check_data)

        cmd = ['qemu-img', 'check', image_file, '--output', 'json']
        cls.logger.debug(cmd)
        try:
            image_info, return_code = cls.qemu_img_cmd_exec(cmd)
            check_result = return_code
            if return_code != 0:
                cls.logger.error('Failed to exec cmd: {}'.format(cmd))
                image_info = {}
            else:
                cls.logger.info('Successfully exec cmd: {}'.format(cmd))
        except StopIteration:
            cls.logger.error('Exit cmd: {}, because of Time Out'.format(cmd))
            image_info = {}
            return_code = 100
        except Exception as exception:
            cls.logger.error('Exit cmd: {}, because of Exception Occured'.format(cmd))
            image_info = {}
            return_code = 99
        finally:
            check_data = cls.read_json_file(check_record_file)
            if not check_data.get('checkResult') or not check_data['checkResult'] in [99, 100]:
                check_data["checkResult"] = check_result
            check_data["imageInfo"] = image_info
            check_data["checkResult"] = check_result
            cls.write_json_file(check_record_file, check_data)
            cls.logger.debug(check_data)

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

        cls.logger.debug(cmd)
        for line in iter(process.stdout.readline, b''):
            data = line.decode('unicode-escape')
            cls.logger.debug(data)
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

        cls.logger.info(compress_output)
        cls.append_write_plain_file(compress_record_file, compress_output)
