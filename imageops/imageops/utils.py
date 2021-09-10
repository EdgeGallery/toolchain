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
import subprocess
from hashlib import md5
from threading import Thread


def async(f):
    def wrapper(*args, **kwargs):
        thr = Thread(target=f, args=args, kwargs=kwargs)
        thr.start()

    return wrapper


class Utils(object):

    @classmethod
    @async
    def get_md5_checksum(cls, image_file, check_record_file):
        m = md5()
        with open(image_file, 'rb') as f:
            while True:
                data = f.read(4096)
                if not data:
                    break
                m.update(data)

        check_record = cls.read_json_file(check_record_file)
        check_record["checksum"] = m.hexdigest()
        cls.write_json_file(check_record_file, check_record)

        return m.hexdigest()

    @staticmethod
    def read_json_file(jsonFile):
        with open(jsonFile, 'rb') as f:
            return json.load(f)

    @staticmethod
    def write_json_file(jsonFile, data):
        with open(jsonFile, 'w') as f:
            f.write(json.dumps(data))
        return

    @classmethod
    @async
    def check_cmd_exec(cls, image_file, check_record_file):
        cmd = 'qemu-img check {} --output json'.format(image_file)
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                             stderr=subprocess.STDOUT)

        image_info = {}
        for line in iter(p.stdout.readline, b''):
            data = line.strip().decode('unicode-escape')
            if data in ['{', '}']:
                continue
            data = str(data).split(':')
            if len(data) != 2:
                continue
            image_info[data[0].strip('"')] = data[1].strip(',').strip().strip('"')

        return_code = p.wait()
        p.stdout.close()

        check_data = cls.read_json_file(check_record_file)
        if return_code != 0:
            check_data["imageInfo"] = {}
            check_data["checkResult"] = return_code
        else:
            check_data["imageInfo"] = image_info
            check_data["checkResult"] = return_code
        cls.write_json_file(check_record_file, check_data)

        return image_info
