/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import zhLocale from 'element-ui/lib/locale/lang/zh-CN'
const cn = {
  appdRes: {
    uploadApp: '上传应用',
    uploadAppImage: '上传App镜像',
    vmDeployConfig: '虚机部署配置',
    appDpMode: '应用负载类型',
    sourceAppdStandard: '源APPD标准',
    targetAppdStandard: '目标APPD标准',
    appPackage: '应用包',
    appDescription: '应用描述',
    chinaU: '联通',
    edgegallery: '社区',
    uploadAppPackage: '上传应用包',
    uploadAppDescription: '上传应用描述',
    vm: '虚拟机',
    container: '容器',
    uploadPackageTip: '(只能上传.csar，.zip格式的文件)',
    uploadMdTip: '只能上传.md格式的文件',
    uploadZipTip: '只能上传.zip格式的文件',
    uploadYamlTip: '只能上传.yaml格式的文件',
    uploadDescriptionTip: '(请上传.md格式的文件，用来介绍App的基本功能、使用场景等)',
    nextStep: '下一步',
    preStep: '上一步',
    submit: '转换',
    imageAddr: '镜像地址',
    imageAddrTip: '请填写镜像下载地址。',
    imageUploadTip: '支持上传镜像，并将此镜像打包到应用中，只能上传.zip格式的文件，镜像存放在应用包的/Image目录下',
    imageUpload: '上传镜像',
    templateUpload: '上传APPD文件',
    showConfig: '可视化配置',
    editOnLine: '在线编辑',
    uploadFile: '上传',
    upoadYaml: '请上传.yaml文件',
    downloadTemplate: '模板下载',
    appdTransFailed: 'appd转换失败',
    transformSuccess: '转换成功，请查看应用包。',
    errorCode1: '操作失败。',
    errorCode100001: '文件过大。',
    errorCode100002: '解压文件太多。',
    errorCode100003: '解压文件失败。',
    errorCode100004: '文件名后缀无效。',
    errorCode100005: '解析文件失败。',
    errorCode100006: '压缩文件失败。',
    errorCode100007: '制作目录失败。',
    errorCode100008: '复制文件失败。',
    errorCode100009: '更新文件失败。',
    errorCode100010: '重命名文件失败。',
    errorCode100011: '文件未找到。',
    errorCode100012: '上传文件失败。',
    errorCode100013: '合并文件失败。',
    errorCode100014: '转换包失败。',
    errorCode100015: '删除文件失败。',
    errorCode100016: '替换文件失败。',
    chooseOneleast: '请确认，镜像地址和镜像包只能二选一。',
    sourceAppdNoEmpty: '源APPD标准不能为空',
    targetAppdNoEmpty: '目标APPD标准不能为空',
    appPackageNoEmpty: '请上传应用包',
    appdTransTool: '应用包转换工具',
    appdTransDes: '应用包转换工具目前支持联通和EdgeGallery的虚机转换，如需支持新的应用包标准，请参考',
    appdDocuments: '扩展文档'

  },
  about: {},
  ...zhLocale
}
export default cn
