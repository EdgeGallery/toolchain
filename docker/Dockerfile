FROM openjdk:8u201-jdk-alpine
WORKDIR /usr/local/toolchain
COPY target/toolchain.jar /usr/local/toolchain/toolchain.jar
EXPOSE 8059
ENTRYPOINT ["java","-jar","toolchain.jar"]
