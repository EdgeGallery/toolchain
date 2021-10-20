#
#    Copyright 2021 Huawei Technologies Co., Ltd.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

FROM swr.cn-north-4.myhuaweicloud.com/eg-common/ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y libguestfs-tools python3-pip linux-image-virtual && \
    rm -rf /var/lib/apt/lists/* && \
    chmod 777 /boot/vmlinuz-*

RUN pip install flask flask_cors gunicorn

ENV HOME=/usr/app
ENV IMAGEOPS_HOME=$HOME/imageops
ENV API_DIR=$IMAGEOPS_HOME/api

WORKDIR $HOME

COPY imageops $IMAGEOPS_HOME

RUN ln -s $IMAGEOPS_HOME /usr/local/lib/python3.8/dist-packages

ENV FLASK_APP ${API_DIR}/routes.py
ENV TMP_PATH /tmp
ENV IMAGE_PATH $HOME/vmImage

ENV UID=166
ENV GID=166
ENV USER_NAME=eguser
ENV GROUP_NAME=eggroup

RUN groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $HOME -s /sbin/nologin -c "Docker image user" $USER_NAME &&\
    chown -hR $USER_NAME:$GROUP_NAME $HOME

USER $USER_NAME

# This port is for flask API in container
EXPOSE 5000

CMD bash $API_DIR/boot.sh
