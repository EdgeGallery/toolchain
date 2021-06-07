# Toolchain Tool chain

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http://jenkins.edgegallery.org/job/toolchain-docker-daily-master/)

The tool chain isMEC DeveloperAn important feature in the developer platform，whenx86PlatformAppWant to get in the carARMPlatform time，The underlying code inevitably needs to be modified or rewritten。
AppThe provider can passMEC DeveloperSource code analysis with integrated tool chain in the developer platform，Locate the source code that needs to be modified and modify it according to the guidance，ConvenienceApp
Deployed onARMplatform。

## Features

Currently，The tool chain service integrates the services provided by the Huawei Kunpeng platform[Porting AdvisorCode migration tool](https://www.huaweicloud.com/kunpeng/software/portingadvisor.html)，The following files can be analyzed：

- C/C++Software source code
- C/C++Software construction project file
- X86Assembly code

## Compile and run

toolchainExternally availablerestfulinterface，Open source basedServiceCombMicroservice framework for development，And integratedSpring Bootframe。due totoolchainNeed to run
relyPorting Advisor，So both need to run in the same environment，Only introduce heretoolchainCompile and run，Porting AdvisorPlease refer to the installation and deployment[Official guidance](https://www.huaweicloud.com/kunpeng/software/portingadvisor.html)。

- ### Environmental preparation
  
    |  Name     | Version   | Link |
    |  ----     | ----  |  ---- |
    | JDK1.8 |1.8xxx or above | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)|
    | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)|
    | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)|
    | Servicecomb Service-Center    | 1.3.0 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)|

- ### Modify the configuration file/src/main/resources/application.properties

    - ConfigurationService Center，Local installationIPYes127.0.0.1，Default port30100，Configuration如下：
    ```
    ### service center config ###
    servicecenter.ip=127.0.0.1
    servicecenter.port=30100
    ```
  
    - ConfigurationPorting Advisor，SuggesttoolchainversusPorting AdvisorInstall in the same environment，Because both need to read and write to the same folder：
    ```
    ### porting advisor parameter config ###
    porting.param.tasksUrl=https://127.0.0.1:8084/porting/api/portadv/tasks/
    porting.param.userUrl=https://127.0.0.1:8084/porting/api/users/
    ```
  
- ### Compile and package
    Pull code from the code repository，defaultmasterBranch
     
     ```
     git clone https://github.com/EdgeGallery/toolchain.git
     ```
 
     Compile and build，Need to rely onJDK1.8，Compiling for the first time will be time-consuming，becausemavenNeed to download all dependent libraries。
 
     ```
     mvn clean install
     ```
 
- ### run
     cdTo package path，byjavastart up：
     ```
     java -jar toolchain.jar
     ```
     Access via browser after launch http://127.0.0.1/30103 You can check whether the service is successfully registered。
