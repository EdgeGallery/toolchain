<!--
  -  Copyright 2021 Huawei Technologies Co., Ltd.
  -
  -  Licensed under the Apache License, Version 2.0 (the "License");
  -  you may not use this file except in compliance with the License.
  -  You may obtain a copy of the License at
  -
  -      http://www.apache.org/licenses/LICENSE-2.0
  -
  -  Unless required by applicable law or agreed to in writing, software
  -  distributed under the License is distributed on an "AS IS" BASIS,
  -  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  -  See the License for the specific language governing permissions and
  -  limitations under the License.
  -->

<template>
  <div class="UploadImage">
    <h3 class="image_title clear">
      <span class="span_lefts">{{ $t('appdRes.imageAddr') }}</span>
      <span class="span_right">
        <el-input
          id="imageAddr"
          v-model="appUploadImage.imageAddr"
          @change="inputValueChange"
          class="imagePath"
        />
        <em class="el-icon-question" />
        {{ $t('appdRes.imageAddrTip') }}
      </span>
    </h3>
    <uploader
      :options="options"
      class="uploader-example"
      @file-complete="fileComplete"
      @file-added="onFileAdded"
      accept=".zip"
    >
      <uploader-unsupport />
      <uploader-drop>
        <uploader-btn>{{ $t('appdRes.imageUpload') }}</uploader-btn>
        <em class="el-icon-question" />
        <span class="imageUploadTipDesc">{{ $t('appdRes.imageUploadTip') }}</span>
      </uploader-drop>
      <uploader-list />
    </uploader>
  </div>
</template>

<script>
import { STANDARDTYPE } from '../../tools/constant.js'
import axios from 'axios'
import { getCookie } from '../../tools/request.js'
export default {
  data () {
    return {
      appdStandardTypes: STANDARDTYPE,
      appUploadImage: {
        step: 'step2',
        imageFile: '',
        imageAddr: ''
      },
      radio: '1',
      options: {
        testChunks: false,
        headers: {},
        forceChunkSize: true,
        simultaneousUploads: 5,
        chunkSize: 8 * 1024 * 1024,
        singleFile: true
      },
      mergerUrl: ''
    }
  },
  created () {
    this.options.headers = { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') }
    let url = window.location.origin
    this.options.target = url + '/mec/appdtranstool/v1/vm/upload'
    this.mergerUrl = url + '/mec/appdtranstool/v1/vm/apps/merge?fileName='
  },
  methods: {
    inputValueChange (val) {
      sessionStorage.setItem('isImageUrl', JSON.stringify(true))
    },
    fileComplete (fileType) {
      const file = arguments[0].file
      let url = this.mergerUrl + file.name + '&guid=' + arguments[0].uniqueIdentifier + '&fileType=image'
      axios.get(url).then(response => {
        this.appUploadImage.imageFile = response.data
        sessionStorage.setItem('isImagePackage', JSON.stringify(true))
      }).catch(error => {
        if (error.response.data.code === 500) {
          let errCode = 'appdRes.errorCode' + error.response.data.retCode
          this.$message({
            duration: 2000,
            message: this.$t(errCode),
            type: 'warning'
          })
        } else {
          this.$message({
            duration: 2000,
            message: this.$t('appdRes.errorCode1'),
            type: 'warning'
          })
        }
      })
    },
    onFileAdded (file) {
      let fileSize = file.file.size / 1024 / 1024 / 1024
      let typeName = file.file.name.substring(file.file.name.lastIndexOf('.') + 1)
      let typeArr = ['zip']
      if (typeArr.indexOf(typeName) === -1 || fileSize > 5) {
        file.ignored = true
        this.$message.warning(this.$t('appdRes.uploadZipTip'))
      }
    },
    parentMsg: function (active) {
      if (active === 1) {
        this.$emit('getStepData', this.appUploadImage)
      }
    }
  }
}
</script>

<style lang="less">
.UploadImage{
  width: 80%;
  margin-left: 8%;
  margin-top: 3%;
  background: #FFFFFF;
  .image_title{
    // font-size: 16px;
    margin-top: 20px;
    width: 100%;
    span{
      margin-bottom: 10px;
    }
    span.span_lefts{
      width: 65px;
      margin-right: 10px;
      font-size: 16px;
    }
    span.span_right{
      width: calc( 80% - 72px);
      font-size: 14px;
      color: #606266;
    }
    .imagePath{
      width: 300px;
    }
  }
  .uploader-example{
    width: 100%;
    .imageUploadTipDesc{
      font-size: 14px;
      color: #606266;
    }
  }
}
.el-icon-question{
  margin-left: 15px;
  font-size: 14px;
  color: #688ef3;
}
</style>
