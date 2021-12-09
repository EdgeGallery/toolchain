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
import re
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
        """
        Get the md5 checksum of the input vm image and write results into file
        """
        try:
            checksum = cls._get_md5_checksum(image_file)
            check_result = 0
            cls.logger.info('Successfully got checksum value: %s', checksum)
        except StopIteration:
            cls.logger.error('Exit checksum Operation because of Time Out')
            check_result = 100
            checksum = 'error'
        except Exception as exception:
            cls.logger.error('Exit CheckSum Operation because of Exception Occured')
            cls.logger.exception(exception)
            check_result = 99
            checksum = 'error'
        finally:
            check_record = cls.read_json_file(check_record_file)
            if check_result == 100:
                check_record['checkResult'] = 100
            if check_result == 99 and check_record['checkResult'] != 63:
                check_record['checkResult'] = 99

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
        """
        Exec qemu-img commands and parse the text results into dict
        """
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
        virtual_size = 0
        try:
            image_info, return_code = cls.qemu_img_cmd_exec(cmd)
            if return_code != 0:
                cls.logger.error('Failed to exec cmd: %s', cmd)
                check_result = 99
                return image_info

            virtual_size = round(int(image_info.get('virtual-size')) / (1024 ** 3), 3)
            disk_size = image_info.get('actual-size')
            if image_info.get('format') != 'qcow2':
                cls.logger.error('Does not accept image with type %s', image_info['format'])
                image_info = {'format': image_info['format'],
                              'virtual_size': virtual_size,
                              'disk_size': disk_size}
                check_result = 63
                return image_info

            check_result = 4
            cls.logger.info('Successfully exec cmd: %s', cmd)
        except StopIteration:
            cls.logger.error('Exit cmd: %s, because of Time Out', cmd)
            image_info = {}
            check_result = 100
            return image_info
        except Exception as exception:
            cls.logger.error('Exit cmd: %s, because of Exception Occured', cmd)
            cls.logger.exception(exception)
            image_info = {}
            check_result = 99
            return image_info
        finally:
            check_data = cls.read_json_file(check_record_file)
            check_data['imageInfo'] = image_info
            if check_data.get('checkResult') != 100:
                if check_result == 4:
                    if check_data.get('checkResult') != 99:
                        check_data['checkResult'] = check_result
                else:
                    check_data['checkResult'] = check_result

            cls.write_json_file(check_record_file, check_data)
            cls.logger.debug(check_data)

        cmd = ['qemu-img', 'check', image_file, '--output', 'json']
        cls.logger.debug(cmd)
        try:
            image_info, return_code = cls.qemu_img_cmd_exec(cmd)
            image_info['virtual_size'] = virtual_size
            image_info['disk_size'] = disk_size
            check_result = return_code
            if return_code != 0:
                cls.logger.error('Failed to exec cmd: %s', cmd)
                return image_info
            cls.logger.info('Successfully exec cmd: %s', cmd)
        except StopIteration:
            cls.logger.error('Exit cmd: %s, because of Time Out', cmd)
            image_info = {}
            check_result = 100
        except Exception as exception:
            cls.logger.error('Exit cmd: %s, because of Exception Occured', cmd)
            cls.logger.exception(exception)
            image_info = {}
            check_result = 99
        finally:
            check_data = cls.read_json_file(check_record_file)
            if check_data.get('checkResult') not in [99, 100]:
                check_data["checkResult"] = check_result
            check_data["imageInfo"] = image_info
            cls.write_json_file(check_record_file, check_data)
            cls.logger.debug(check_data)

        return image_info

    @classmethod
    @timeout_decorator.timeout(TIMEOUT, timeout_exception=StopIteration, use_signals=False)
    def _virt_sparsify_cmd_exec(cls, cmd, compress_record_file):
        check_tmpdir = True
        process = subprocess.Popen(cmd, shell=False, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT)
        cls.logger.debug(cmd)
        for line in iter(process.stdout.readline, b''):
            data = line.decode('unicode-escape')
            cls.logger.debug(data)
            if 'Exiting because --check-tmpdir=fail was set' in data:
                check_tmpdir = False
            if 'Copy to destination and make sparse' in data:
                cls.append_write_plain_file(compress_record_file,
                                            'Medium timestamp: {}\n'.format(time.time()))
            cls.append_write_plain_file(compress_record_file, data)
        return_code = process.wait()
        process.stdout.close()
        return return_code, check_tmpdir

    @classmethod
    @imageasync
    def compress_cmd_exec(cls, input_image, output_image, compress_record_file):
        """
        Exec virt-sparsity commad to compress and convert the given vm image to qcow2
        """
        cmd = ['virt-sparsify', input_image, '--compress', '--convert', 'qcow2', output_image,
               '--machine-readable', '--check-tmpdir=fail']

        try:
            cls.append_write_plain_file(compress_record_file,
                                        'Start timestamp: {}\n'.format(time.time()))
            return_code, check_tmpdir = cls._virt_sparsify_cmd_exec(cmd, compress_record_file)

            if return_code == 0:
                compress_output = 'Compress Completed\n'
            elif not check_tmpdir:
                compress_output = 'Compress Exiting because of No enouth space left\n'
            else:
                compress_output = 'Compress Failed\n'
        except StopIteration:
            compress_output = 'Compress Time Out\n'
        except Exception as exception:
            compress_output = 'Compress Failed with exception: {}'.format(exception)

        cls.logger.info(compress_output)
        cls.append_write_plain_file(compress_record_file, compress_output)
        cls.append_write_plain_file(compress_record_file,
                                    'End timestamp: {}\n'.format(time.time()))

    @classmethod
    def get_compress_rate(cls, compress_record_file):
        """
        Estimate the rate of compress by parsing the record file
        """
        rate_info = {'begin': {'sig': 'Fill free space', 'base': 0, 'end': 0.3},
                     'medium':
                         {'sig': 'Copy to destination and make sparse', 'base': 0.3, 'end': 0.9},
                     'end': {'sig': 'Sparsify operation', 'base': 0.9, 'end': 1.0}
                    }
        rate = rate_info.get('begin').get('base')
        process_status = {1: False, 2: False, 3: False}
        with open(compress_record_file, 'r') as compress_file:
            begin = rate_info.get('begin')
            medium = rate_info.get('medium')
            end = rate_info.get('end')
            for line in compress_file:
                if 'Start timestamp' in line:
                    begin['start_time'] = int(re.search(r'\d+', line).group())
                    continue
                if 'Medium timestamp' in line:
                    medium['start_time'] = int(re.search(r'\d+', line).group())
                    continue
                cls.update_process_status(line, process_status, rate_info)
                if process_status.get(3):
                    rate = end.get('end')
                    return rate
                if process_status.get(2):
                    cost_one = medium.get('start_time') - begin.get('start_time')
                    cost_two = int(time.time()) - medium.get('start_time')
                    rate = round(medium.get('base') + cost_two / (2 * cost_one), 3)
                    rate = min(rate, medium.get('end'))
                elif process_status.get(1):
                    rate_one_num = re.findall(r'\d+', line)
                    if len(rate_one_num) != 2:
                        continue
                    rate_one = float(rate_one_num[0])/float(rate_one_num[1])
                    base = begin.get('base')
                    rate = round(base + (begin.get('end') - base) * rate_one, 3)
        return rate

    @classmethod
    def update_process_status(cls, line, process_status, rate_info):
        """
        Update compress process by parsing the input line.
        """
        if rate_info.get('begin').get('sig') in line:
            process_status[1] = True
        if rate_info.get('medium').get('sig') in line:
            process_status[2] = True
        if rate_info.get('end').get('sig') in line:
            process_status[3] = True

    @classmethod
    def check_compress_requires(cls, input_image, check_path, free_rate=0.2):
        """
        Check whether the space left is enough for compressing operation.
        """
        disk = os.statvfs(check_path)
        disk_size = disk.f_bsize * disk.f_blocks / (1024 ** 3)
        disk_free = disk.f_bsize * disk.f_bfree / (1024 ** 3)

        cmd = ['qemu-img', 'info', input_image, '--output', 'json']
        cls.logger.debug('Get virtual size of image %s', input_image)
        virtual_size = 0
        image_info, return_code = cls.qemu_img_cmd_exec(cmd)

        if return_code != 0 or not image_info.get('virtual-size'):
            cls.logger.error('Failed to get virtual size of image %s', input_image)
            return False

        virtual_size = round(int(image_info.get('virtual-size')) / (1024 ** 3), 3)
        if virtual_size >= disk_free:
            cls.logger.error('Disk space left is smaller than virtual size of image %s',
                             input_image)
            return False
        new_free_rate = round((disk_free - virtual_size) / disk_size, 3)
        if new_free_rate < free_rate:
            cls.logger.error('Free disk after compress will be %s, smaller than required %s',
                             new_free_rate, free_rate)
            return False
        return True
