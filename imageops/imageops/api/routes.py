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

from imageops.logger import Logger
from imageops.server import Server

app = Flask(__name__)
CORS(app)
logger = Logger(__name__).get_logger()

@app.route('/api/v1/vmimage/check', methods=['POST'])
def check_vm_image():
    input_image_name = request.json.get('inputImageName')
    if not input_image_name:
        msg = 'Lacking inputImageName in request body.'
        logger.error(msg)
        return msg, 500

    try:
        request_id = uuid.uuid1()
        server = Server(request_id)
        image = os.path.join(server.image_path, input_image_name)
        status, msg = server.check_vm_image(image)
        return jsonify({'status': status, 'msg': msg, 'requestId': request_id}), 200
    except Exception as exception:
        logger.error(exception)
        return str(exception), 500


@app.route('/api/v1/vmimage/check/<request_id>', methods=['GET'])
def get_check_status(request_id):
    try:
        server = Server(request_id)
        status, msg, check_info = server.get_check_status()
        return jsonify({'status': status, 'msg': msg, 'checkInfo': check_info}), 200
    except Exception as exception:
        logger.error(exception)
        return str(exception), 500


@app.route('/api/v1/vmimage/compress', methods=['POST'])
def compress_vm_image():
    input_image_name = request.json.get('inputImageName')
    if not input_image_name:
        msg = 'Lacking inputImageName in request body.'
        logger.error(msg)
        return msg, 500

    output_image_name = request.json.get('outputImageName')
    if not output_image_name:
        msg = 'Lacking outputImageName in request body.'
        logger.error(msg)
        return msg, 500

    try:
        request_id = uuid.uuid1()
        server = Server(request_id)
        input_image = os.path.join(server.image_path, input_image_name)
        output_image = os.path.join(server.image_path, output_image_name)
        status, msg = server.compress_vm_image(input_image, output_image)
        return jsonify({'status': status, 'msg': msg, 'requestId': request_id}), 200
    except Exception as exception:
        logger.error(exception)
        return str(exception), 500


@app.route('/api/v1/vmimage/compress/<request_id>', methods=['GET'])
def get_compress_status(request_id):
    try:
        server = Server(request_id)
        status, msg, rate = server.get_compress_status()
        return jsonify({'status': status, 'msg': msg, 'rate': rate}), 200
    except Exception as exception:
        logger.error(exception)
        return str(exception), 500


if __name__ == '__main__':
    app.run()
