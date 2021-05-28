# Toolchain 工具链

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http://jenkins.edgegallery.org/job/toolchain-docker-daily-master/)

工具链是MEC Developer开发者平台中的一个重要特性，当x86平台的App想要上车ARM平台时，底层的代码不可避免的需要进行修改或重写。
App提供者可以通过MEC Developer开发者平台中集成的工具链进行源代码分析，定位需要修改的源代码并根据指导意见进行修改，方便App
部署在ARM平台。

## 功能介绍

目前，工具链服务集成了华为鲲鹏平台提供的[Porting Advisor代码迁移工具](https://www.huaweicloud.com/kunpeng/software/portingadvisor.html)，可以对以下文件进行分析：

- C/C++软件源码
- C/C++软件构建工程文件
- X86汇编代码

## 编译运行

toolchain对外提供restful接口，基于开源的ServiceComb微服务框架进行开发，并且集成了Spring Boot框架。由于toolchain的运行需要
依赖Porting Advisor，所以两者需要运行在同一环境下，此处仅介绍toolchain的编译运行，Porting Advisor的安装部署请参考[官方指导](https://www.huaweicloud.com/kunpeng/software/portingadvisor.html)。

- ### 环境准备
  
    |  Name     | Version   | Link |
    |  ----     | ----  |  ---- |
    | JDK1.8 |1.8xxx or above | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)|
    | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)|
    | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)|
    | Servicecomb Service-Center    | 1.3.0 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)|

- ### 修改配置文件/src/main/resources/application.properties

    - 配置Service Center，本地安装IP是127.0.0.1，默认端口30100，配置如下：
    ```
    ### service center config ###
    servicecenter.ip=127.0.0.1
    servicecenter.port=30100
    ```
  
    - 配置Porting Advisor，建议toolchain与Porting Advisor安装在同一个环境，因为两者需要对同一个文件夹进行读写：
    ```
    ### porting advisor parameter config ###
    porting.param.tasksUrl=https://127.0.0.1:8084/porting/api/portadv/tasks/
    porting.param.userUrl=https://127.0.0.1:8084/porting/api/users/
    ```
  
- ### 编译打包
    从代码仓库拉取代码，默认master分支
     
     ```
     git clone https://github.com/EdgeGallery/toolchain.git
     ```
 
     编译构建，需要依赖JDK1.8，首次编译会比较耗时，因为maven需要下载所有的依赖库。
 
     ```
     mvn clean install
     ```
 
- ### 运行
     cd到打包路径，通过java启动：
     ```
     java -jar toolchain.jar
     ```
     启动后通过浏览器访问 http://127.0.0.1/30103 可以查看服务是否注册成功。
