FROM ubuntu:20.04

MAINTAINER Dan Xu <xudan16@huawei.com>

RUN apt-get update

ENV HOME=/usr/app
ENV API_DIR=$HOME/api
ENV UID=166
ENV GID=166
ENV USER_NAME=eguser
ENV GROUP_NAME=eggroup
ENV ENV="/etc/profile"

RUN groupadd -r -g $GID $GROUP_NAME &&\
    useradd -r -u $UID -g $GID -d $HOME -s /sbin/nologin -c "Docker image user" $USER_NAME

WORKDIR $HOME

COPY imageops $HOME

ENV FLASK_APP ${API_DIR}/routes.py

# This port is for flask API in container
EXPOSE 5000
# This port is for Swagger UI in container
EXPOSE 80

CMD ["sh", "-c", "$API_DIR/boot.sh"]
