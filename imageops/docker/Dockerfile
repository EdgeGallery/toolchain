FROM swr.cn-north-4.myhuaweicloud.com/eg-common/ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
    apt-get install -y qemu-utils libguestfs-tools

RUN apt-get install -y python3-pip linux-image-generic

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

# This port is for flask API in container
EXPOSE 5000

CMD bash $API_DIR/boot.sh
