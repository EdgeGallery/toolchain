FROM python:3.6.15-slim

MAINTAINER Dan Xu <xudan16@huawei.com>

RUN apt-get update && \
    apt-get install -y qemu-utils libguestfs-tools

RUN pip install flask flask_cors gunicorn

ENV HOME=/usr/app
ENV IMAGEOPS_HOME=$HOME/imageops
ENV API_DIR=$IMAGEOPS_HOME/api
#ENV UID=166
#ENV GID=166
#ENV USER_NAME=eguser
#ENV GROUP_NAME=eggroup
#ENV ENV="/etc/profile"
#
#RUN groupadd -r -g $GID $GROUP_NAME &&\
#    useradd -r -u $UID -g $GID -d $HOME -s /sbin/nologin -c "Docker image user" $USER_NAME

WORKDIR $HOME

COPY imageops $IMAGEOPS_HOME

RUN ln -s $IMAGEOPS_HOME /usr/local/lib/python3.6/imageops

ENV FLASK_APP ${API_DIR}/routes.py
ENV TMP_PATH /tmp
ENV IMAGE_PATH $HOME/vmImage

# This port is for flask API in container
EXPOSE 5000

CMD bash $API_DIR/boot.sh
