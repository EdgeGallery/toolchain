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

import enLocale from 'element-ui/lib/locale/lang/en'
const en = {
  appdRes: {
    uploadApp: 'upload App',
    uploadAppImage: 'upload App image',
    vmDeployConfig: 'VM deployment configuration',
    appDpMode: 'App load type',
    sourceAppdStandard: 'Source APPD standard',
    targetAppdStandard: 'Target APPD standard',
    appPackage: 'App Package',
    appDescription: 'App Description',
    chinaU: 'chinaUnion',
    edgegallery: 'edgegallery',
    uploadAppPackage: 'Upload App package',
    uploadAppDescription: 'Upload App description',
    vm: 'VM',
    container: 'Container',
    uploadPackageTip: 'Only files in .csar and .zip format can be uploaded',
    uploadDescriptionTip: 'Please upload a file in .md format to introduce the basic functions and usage scenarios of the App, etc.',
    nextStep: 'Next step',
    preStep: 'Previous',
    submit: 'Confirm',
    imageAddr: 'Image address',
    imageAddrTip: 'Please fill in the URL of the image download address, or the path in the app package',
    imageUploadTip: 'Supports uploading images and packaging this image into the app. Only files in .zip format can be uploaded. The image is stored in the /Image directory of the app package.',
    imageUpload: 'Upload image',
    templateUpload: 'Upload template',
    showConfig: 'Visual configuration',
    editOnLine: 'Online editing',
    uploadFile: 'Upload',
    upoadYaml: 'Please upload .yaml file',
    downloadTemplate: 'Template download',
    appdTransFailed: 'appd conversion failed',
    errorCode0: 'operate successful',
    errorCode1: 'operate failed',
    errorCode100001: 'file size is too big.',
    errorCode100002: 'too many files to unzip.',
    errorCode100003: 'unzip file failed.',
    errorCode100004: 'file name postfix is invalid.',
    errorCode100005: 'parse file failed.',
    errorCode100006: 'compress file failed.',
    errorCode100007: 'make directory failed.',
    errorCode100008: 'copy file failed.',
    errorCode100009: 'update file failed.',
    errorCode100010: 'rename file failed.',
    errorCode100011: 'file not found.',
    errorCode100012: 'upload file failed.',
    errorCode100013: 'merge file failed.',
    errorCode100014: 'transform package failed.',
    errorCode100015: 'delete file failed.',
    errorCode100016: 'replace file failed.',
    chooseOneleast: 'Please select at least one of the image address and the image package.',
    sourceAppdNoEmpty: 'The source APPD standard cannot be empty',
    targetAppdNoEmpty: 'The target APPD standard cannot be empty',
    appPackageNoEmpty: 'Please upload the App package',
    appdTransTool: 'App package transfer tool',
    appdTransDes: 'The app package transfer tool currently supports Unicom and VM transfer, and will be expanded to support transfer of more app package standards'
  },
  about: {
  },
  ...enLocale
}
export default en
