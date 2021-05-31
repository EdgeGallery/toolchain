# AppdTransTool
该工具实现不同标准的应用包的转换，促进应用共享。

## 工具转换方案 
将各个标准的应用包模板、应用包信息定义、包模板转换规则录入系统，只支持系统中有的标准转换。
在界面选择源、目标标准，上传需要的文件，通过标准模板、包定义、模板转换规则等进行转换，虚机部署信息，如AZ、主机组、启动脚本可以通过界面输入，也可以在线编译部署文件，前台将编辑好的部署文件传给后台替换，因此前台需要保存各个标准的部署模板文件。

![输入图片说明](https://images.gitee.com/uploads/images/2021/0531/184421_6e8d8544_8354563.png "files.png")

#### 应用包模板

将各个标准的应用包模板放在配置文件路径下，其中可变参数使用变量标识，如：

```
metadata:
app_name: {app_name}
app_provider: {app_provider}
app_package_version: {app_package_version}
app_release_data_time: {app_release_data_time}
app_type: {app_type}
app_package_description: {app_package_description}
me_iaas_type: openstack
product_vendor_id: {app_provider}
product_type: {app_type}
product_id: {product_id}
version: {app_package_version}
product_vendor: {app_provider}
product_name: {app_name}

Source: MEAD/openstack/templates/deploy.yaml
Algorithm: SHA-256
Hash: {hash_appd}

Source: MEAD/mep/config.yaml
Algorithm: SHA-256
Hash: {hash_mep}
```

#### 应用包信息定义

应用包转换时，通过应用包定义，可以获取可变参数的值，如：

```
{
    "appInfo": {
        "app_name": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_name"
        },
        "app_provider": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_provider"
        },
        "app_package_version": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_package_version"
        },
        "app_release_data_time": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_release_data_time"
        },
        "app_type": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_type"
        },
        "app_package_description": {
            "filePath": "/",
            "fileType": ".mf",
            "location": "app_package_description"
        }
    },
    "computeInfo": {
        "vm_name": {
            "filePath": "/MEAD/openstack/template",
            "fileType": ".yaml",
            "location": "topology_template.node_templates.APP1.properties.name"
        },
        "storagesize": {
            "filePath": "/MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vstorage"
        },
        "memorysize": {
            "filePath": "/MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vmemory"
        },
        "vcpu": {
            "filePath": "/MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vcpu"
        },
        "image_name": {
            "filePath": "/MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.image_name"
        }
    }
}
```

包定义中主要包含应用信息appInfo和虚机资源信息computeInfo，定义中指定了该参数所在文件的路径、文件类型以及在文件中的位置，如果同一个路径下有同类型的多个文件，可以有排他列表excludeFile，如：

```
        "image_name": {
            "filePath": "/APPD/Definition",
            "fileType": ".yaml",
            "location": "topology_template.node_templates.EMS_VDU1.properties.sw_image_data.name",
            "excludeFile": "nfv_vnfd_types_v1_0.yaml"
        }
```

如此，通过包定义，可以从源标准获取到需要参数的值。

#### 应用包模板转换规则

该规则规定转换到目标标准时需要进行哪些操作，如更新文件列表updateFiles、需要重命名的文件renameFiles，需要生成参数列表generateValues、是否需要对文件进行hash校验isNeedHashCheck，如：

```
{
    "updateFiles": [
        {
            "file": "/app-name.mf",
            "envs": [
                "{app_name}",
                "{app_provider}",
                "{app_package_version}",
                "{app_release_data_time}",
                "{app_type}",
                "{app_package_description}",
                "{product_id}"
            ]
        },
        {
            "file": "/MEAD/openstack/templates/deploy.yaml",
            "envs": [
                "{image_name}",
                "{vm_name}"
            ]
        },
        {
            "file": "/MEAD/openstack/values.yaml",
            "envs": [
                "{memorysize}",
                "{vcpu}",
                "{storagesize}",
                "{image_name}"
            ]
        }
    ],
    "renameFiles": [
        {
            "file": "/app-name.mf",
            "newName": "{app_name}"
        }
    ],
    "generateValues": [
        {
            "item": "product_id",
            "type": "uuid"
        }
    ],
    "isNeedHashCheck": "true"
}
```

该规则定义了具体需要操作的文件、替换的变量、生成的变量等，还有需要压缩的文件zipFiles、替换的文件replaceFiles、外部输入变量inputs，如：

```
    "replaceFiles": [
        "Artifacts/Docs/template.md"
    ],
    "zipFiles": [
        {
            "path": "/APPD/",
            "zipName": "{app_name}"
        }
    ],
    "inputs": [
        {
            "item": "az",
            "type": "string"
        },
        {
            "item": "flavor",
            "type": "string"
        },
        {
            "item": "bootdata",
            "type": "string"
        },
        {
            "item": "image_path",
            "type": "string"
        }
    ]
```

各个规则的操作顺序：replaceFiles、generateValues、inputs、updateFiles、renameFiles、zipFiles、hash文件校验（只针对mf文件中配置的source文件）。

根据该规则可以转换为目标标准的应用包。

### 文件上传

不管大文件还是小文件，都采用分片上传方式，点击上传后，都先将文件保存到本地服务器，后台返回给前台文件路径，文件上传接口参考appstore。
文件保存路径：/usr/app/transtool, 子级目录有package、doc、image、deploy，分别存放上传的文件。文件转换完后需要清理目录。

### 接口变更


| URL                                  | Method | Change Type | request                                                      | response                                                     |
| ------------------------------------ | ------ | ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| /mec/appdtranstool/v1/vm/templates/ | GET   | Add         |  无                                                          | ["ChinaUnicom", "EdgeGallery"]  |
| /mec/appdtranstool/v1/vm/upload | POST   | Add         | 同/mec/appstore/v1/apps/upload接口                           | {<br />    "upload file success."<br />}  <br />             |
| /mec/appdtranstool/v1/vm/merge  | GET    | Add         | 同/mec/appstore/v1/apps/merge接口                            | {<br />    "file address"<br />}                             |
| /mec/appdtranstool/v1/vm/trans  | POST   | Add         | {<br />    "sourceAppd" : string,<br />    "destAppd" : string, <br />    "appFile" : string,<br />    "docFile" : string,<br />    "imageFile": string,<br />    "imagePath": string,<br />    "az": string, <br />    "flavor": string,<br />    "bootdata": string,<br />    "deployFile":string<br />} | application/octet-stream  {      binary output.  }<br />返回转换后的应用包 |


### 界面设计

![输入图片说明](https://images.gitee.com/uploads/images/2021/0531/184454_e0bc5473_8354563.png "appd-1.png")
![输入图片说明](https://images.gitee.com/uploads/images/2021/0531/184507_a765b1dc_8354563.png "appd-2.png")
![输入图片说明](https://images.gitee.com/uploads/images/2021/0531/184517_7bf47847_8354563.png "appd-3.png")
