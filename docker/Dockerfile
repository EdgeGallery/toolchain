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

FROM openjdk:8u201-jdk-alpine
WORKDIR /usr/local/toolchain
COPY target/toolchain.jar /usr/local/toolchain/toolchain.jar
EXPOSE 8059
ENTRYPOINT ["java","-jar","toolchain.jar"]
