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

from pathlib import Path


class Server(object):

    def __init__(self, requestId=None):
        if not requestId:
            raise Exception('Lacking requestId\n')
        self.requestId = requestId

    def checkVMImage(self, inputImage=None):
        if not inputImage:
            raise Exception('No image is given\n')

        image = Path(inputImage)
        if not image.is_file():
            raise Exception('Given image {} is not exist\n'.format(inputImage))

        imageInfo = {"image-end-offset": 564330496,
                     "compressed-clusters": 19368,
                     "total-clusters": 36032,
                     "check-errors": 0,
                     "allocated-clusters": 21637,
                     "filename": "ubuntu-20.4-ssh.img",
                     "format": "qcow2",
                     "fragmented-clusters": 19472
                    }
        status = 0
        msg = "Check completed, the image is (now) consistent"

        return status, msg, imageInfo

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
        return status, msg
