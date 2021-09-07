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

#!flask/bin/python

import os
import uuid

from flask import Flask, jsonify, request
from flask_cors import CORS

from imageops.server import Server

app = Flask(__name__)
CORS(app)


@app.route('/api/v1/vmimage/check/<fileName>', methods=['GET'])
def checkVMImage(fileName):
    if os.getenv('IMAGE_PATH'):
        imagePath = os.getenv('IMAGE_PATH')
    else:
        return 'No IMAGE_PATH found in env.\n', 500

    try:
        requestId = uuid.uuid1()
        image = os.path.join(imagePath, fileName)
        server = Server(requestId)
        status, msg, imageInfo = server.checkVMImage(image)
        return jsonify({'status': status, 'msg': msg, 'imageInfo': imageInfo, 'requestId': requestId}), 200
    except Exception as e:
        return str(e), 500


@app.route('/api/v1/vmimage/compress', methods=['POST'])
def compressVMImage():
    if os.getenv('IMAGE_PATH'):
        imagePath = os.getenv('IMAGE_PATH')
    else:
        return 'No IMAGE_PATH found in env.\n', 500

    inputImageName = request.json.get('inputImageName')
    if not inputImageName:
        return 'Lacking inputImageName in request body.\n', 500

    outputImageName = request.json.get('outputImageName')
    if not outputImageName:
        return 'Lacking outputImageName in request body.\n', 500

    try:
        requestId = uuid.uuid1()
        inputImage = os.path.join(imagePath, inputImageName)
        outputImage = os.path.join(imagePath, outputImageName)
        server = Server(requestId)
        status, msg = server.compressVMImage(inputImage, outputImage)
        return jsonify({'status': status, 'msg': msg, 'requestId': requestId}), 200
    except Exception as e:
        return str(e), 500


@app.route('/api/v1/vmimage/compress/<requestId>', methods=['GET'])
def getCompressStatus(requestId):
    try:
        server = Server(requestId)
        status, msg = server.getCompressStatus()
        return jsonify({'status': status, 'msg': msg}), 200
    except Exception as e:
        return str(e), 500


if __name__ == '__main__':
    app.run()
