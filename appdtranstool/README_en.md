# AppdTransTool
The tool realizes the conversion of application packages of different standards，Promote application sharing。

## Tool conversion plan 
Application package templates of various standards、Application package information definition、Package template conversion rule entry system，Only support standard conversion in the system。
Select the source in the interface、Target standard，Upload the required files，Pass standard template、Package definition、Template conversion rules, etc.，Virtual machine deployment information，Such asAZ、Host group、The startup script can be entered through the interface，You can also compile the deployment file online，The front desk transfers the edited deployment file to the backend for replacement，Therefore, the front desk needs to save various standard deployment template files。

![Enter picture description](https://images.gitee.com/uploads/images/2021/0531/184421_6e8d8544_8354563.png "files.png")

#### Application package template

Put each standard application package template under the configuration file path，Variable parameters use variable identification，Such as：

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

#### Application package information definition

When applying package conversion，Defined by application package，You can get the value of a variable parameter，Such as：

```
{
    "appInfo": {
        "app_name": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_name"
        },
        "app_provider": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_provider"
        },
        "app_package_version": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_package_version"
        },
        "app_release_data_time": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_release_data_time"
        },
        "app_type": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_type"
        },
        "app_package_description": {
            "filePath": "",
            "fileType": ".mf",
            "location": "app_package_description"
        }
    },
    "computeInfo": {
        "vm_name": {
            "filePath": "MEAD/openstack/templates",
            "fileType": ".yaml",
            "location": "topology_template.node_templates.APP1.properties.name",
            "isZip": "false"
        },
        "storagesize": {
            "filePath": "MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vstorage",
            "isZip": "false"
        },
        "memorysize": {
            "filePath": "MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vmemory",
            "isZip": "false"
        },
        "vcpu": {
            "filePath": "MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.vcpu",
            "isZip": "false"
        },
        "image_name": {
            "filePath": "MEAD/openstack",
            "fileType": ".yaml",
            "location": "APP1.image_name",
            "isZip": "false"
        }
    }
}
```

The package definition mainly contains application informationappInfoAnd virtual machine resource informationcomputeInfo，The path of the file where the parameter is located is specified in the definition、File type and location in the file，If there are multiple files of the same type under the same path, Can have an exclusive excludeFile, if the file exists in a zip file, sub path need to be known, Such as：

```
        "vm_name": {
            "filePath": "APPD",
            "fileType": ".yaml",
            "location": "topology_template.node_templates.EMS_VDU1.properties.name",
            "excludeFile": "nfv_vnfd_types_v1_0.yaml",
            "isZip": "true",
            "subPath": "Definition"
        }
```

in this way，Defined by package，The value of the required parameter can be obtained from the source standard。

#### Application package template conversion rules

This rule stipulates what needs to be done when transitioning to the target standard，E.g. update file listupdateFiles、Files that need to be renamedrenameFiles，Need to generate parameter listgenerateValues、Do you need to performhashcheckisNeedHashCheck，Such as：

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

The rule defines the specific files that need to be manipulated、Replaced variable、Generated variables, etc.，There are also files that need to be compressedzipFiles、Replaced filereplaceFiles、External input variableinputs，Such as：

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

The order of operations of each rule：replaceFiles、generateValues、inputs、updateFiles、renameFiles、zipFiles、hashDocument verification（Only formfConfigured in the filesourcefile）。

According to this rule, it can be converted into an application package of the target standard。

### File Upload

Regardless of large files or small files，All use multiple upload methods，After clicking upload，Save the file to the local server first，File path returned from the background to the foreground，File upload interface referenceappstore。
File save path：/usr/app/transtool, Sub-directories havepackage、doc、image、deploy，Store uploaded files separately。The directory needs to be cleaned up after the file is converted。

### Interface change


| URL                                  | Method | Change Type | request                                                      | response                                                     |
| ------------------------------------ | ------ | ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| /mec/appdtranstool/v1/vm/templates/ | GET   | Add         |  no                                                          | ["ChinaUnicom", "EdgeGallery"]  |
| /mec/appdtranstool/v1/vm/upload | POST   | Add         | with/mec/appstore/v1/apps/uploadinterface                           | {<br />    "upload file success."<br />}  <br />             |
| /mec/appdtranstool/v1/vm/merge  | GET    | Add         | {<br />    "fileName" : string,<br />    "guid" : string, <br />    "fileType" : string<br />}<br />fileTypeThe value of：package、doc、image、deploy。 | {<br />    "file address"<br />}                             |
| /mec/appdtranstool/v1/vm/trans  | POST   | Add         | {<br />    "sourceAppd" : string,<br />    "destAppd" : string, <br />    "appFile" : string,<br />    "docFile" : string,<br />    "imageFile": string,<br />    "imagePath": string,<br />    "az": string, <br />    "flavor": string,<br />    "bootdata": string,<br />    "deployFile":string<br />} | application/octet-stream  {      binary output.  }<br />Return the converted application package |


### interface design

![Enter picture description](https://images.gitee.com/uploads/images/2021/0531/184454_e0bc5473_8354563.png "appd-1.png")
![Enter picture description](https://images.gitee.com/uploads/images/2021/0531/184507_a765b1dc_8354563.png "appd-2.png")
![Enter picture description](https://images.gitee.com/uploads/images/2021/0531/184517_7bf47847_8354563.png "appd-3.png")
