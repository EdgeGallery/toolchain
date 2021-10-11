### 虚机镜像操作工具

本仓库代码提供虚机镜像相关操作，目前主要包含如下两个操作：
- check：检查虚机镜像信息，包括计算checksum，以及镜像基本大小、类型等信息，此操作仅能接受qcow2镜像格式
- compress：对镜像进行瘦身操作，主要包括将qcow2或者raw镜像进行稀疏化、磁盘清理等操作，精简镜像，减小镜像大小，最后输出qcow2镜像

### 使用方法

本虚机镜像操作工具可以通过容器方式进行部署，对外提供Restful API，供其他前、后台调用。

1. 运行imageops容器

```
docker run -d -v <host-vm-image-path>:/usr/app/vmImage -p <host-port>:5000 swr.cn-north-4.myhuaweicloud.com/edgegallery/imageops:<image-tag>
```

- `<host-vm-image-path>`：本机目录，用于存放虚机镜像，并映射到容器中的特定路径
- `<host-port>`：本机端口，用于对外提供此服务，可以为任意的空闲端口，并映射到容器中的5000端口
- `<image-tag>`：imageops镜像tag，当前有latest和v1.3.0两个版本可供选择

2. 在`<host-vm-image-path>`里存放一个供测试的镜像，以下以一个ubuntu-20.04镜像为例

```
cd <host-vm-image-path>
wget http://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64.img
```

3. 发送check镜像请求

```
curl -X POST -H "Content-Type: application/json" -d '{"inputImageName": "focal-server-cloudimg-amd64.img"}'  http://127.0.0.1:<host-port>/api/v1/vmimage/check
```

执行上述POST请求之后，会得到如下返回，表示check请求已收到，正在check过程中，并返回一个requestId供查询使用。

```
{"msg":"Check In Progress","requestId":"ec6ab748-2a6d-11ec-a684-0242ac110002","status":0}
```

4. 发送check状态查询请求

```
curl -X GET http://127.0.0.1:<host-port>/api/v1/vmimage/check/<requestId>
```

使用第3步的POST请求获得到的<requestId>进行查询，直到得到如下返回结果，显示check成功，或者check失败。


```
{"checkInfo":{"checkResult":0,"checksum":"565a2c24253bb259c0bd447f814db579","imageInfo":{"allocated-clusters":"21740","check-errors":"0","compressed-clusters":"19401","filename":"/usr/app/vmImage/focal-server-cloudimg-amd64.img","format":"qcow2","fragmented-clusters":"19492","image-end-offset":"567017472","total-clusters":"36032"}},"msg":"Check Completed, the image is (now) consistent","status":0}
```

5. 发送compress镜像请求

```
curl -X POST -H "Content-Type: application/json" -d '{"inputImageName": "focal-server-cloudimg-amd64.img", "outputImageName": "New-focal-server-cloudimg-amd64.img.img"}'  http://127.0.0.1:<host-port>/api/v1/vmimage/compress
```

执行上述POST请求之后，会得到如下返回，表示compress请求已收到，正在compress过程中，并返回一个requestId供查询使用。

```
{"msg":"Compress In Progress\n","requestId":"64a9ff7e-2a6f-11ec-a684-0242ac110002","status":0}
```

6. 发送compress状态查询请求

```
curl -X GET  http://127.0.0.1:<host-port>/api/v1/vmimage/compress/<requestId>
```

使用第5步的POST请求获得到的<requestId>进行查询，直到得到如下返回结果，显示compress成功，或者compress失败。

```
{"msg":"Compress Completed","rate":1,"status":0}
```





