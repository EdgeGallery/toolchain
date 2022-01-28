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
    <h3
      class="image_title clear"
      :class="{'image_titleen':(this.language==='中文')}"
    >
      <span class="span_lefts">{{ $t('appdRes.imageAddr') }}</span>
      <span class="span_right">
        <el-input
          id="imageAddr"
          v-model="appUploadImage.imageAddr"
          @input="inputValueChange"
          class="imagePath"
          :class="{'imagePathen':(this.language==='中文')}"
        />
      </span>
    </h3>
    <p
      class="careful"
      :class="{'carefulen':(this.language==='中文')}"
    >
      {{ $t('appdRes.imageAddrTip') }}
    </p>
    <uploader
      :options="options"
      class="uploader-example"
      :class="{'image_titleen':(this.language==='中文')}"
      @file-complete="fileComplete"
      @file-added="onFileAdded"
      @file-removed="deleteFile"
      accept=".zip"
    >
      <uploader-unsupport />
      <div style="display:flex;">
        <span
          class="span_lefts lefts2"
          :class="{'lefts2en':(this.language==='中文')}"
        >{{ $t('appdRes.imageUpload') }}</span>
        <uploader-drop
          class="uploadBtn"
          :class="{'uploads':(this.language==='中文')}"
        >
          <uploader-btn
            id="uploadImg"
            :class="{'uploadBtnen':(this.language==='中文')}"
          >
            {{ $t('appdRes.imageUpload') }}
          </uploader-btn>
          <em class="el-icon-question" />
          <span class="imageUploadTipDesc">{{ $t('appdRes.imageUploadTip') }}</span>
        </uploader-drop>
      </div>
      <uploader-list :class="{'uploader-listen':(this.language==='中文')}" />
    </uploader>
  </div>
</template>

<script>
import { STANDARDTYPE } from '../../tools/constant.js'
import axios from 'axios'
import { getCookie } from '../../tools/request.js'
export default {
  props: {
    language: {
      type: String,
      required: true
    }
  },
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
      mergerUrl: '',
      imageUpload: false
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
      if (val === '') {
        sessionStorage.removeItem('isImageUrl')
      } else {
        sessionStorage.setItem('isImageUrl', JSON.stringify(true))
      }
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
    deleteFile (file, fileList) {
      sessionStorage.removeItem('isImagePackage')
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
  margin-top: 40px;
  width: 100%;
  padding: 25px 0 0 100px;
  .uploader-file{
  border: none !important;
  margin-left: 130px !important;
}
  span.span_lefts{
      font-size: 16px;
      font-family: HarmonyHeiTi;
      font-weight: 400;
      color: #fff;
      line-height: 24px;
    }
    .lefts2{
      margin-top: 10px;
    }
    .span_lefts.lefts2en{
       width: 168px;
       margin-left: 0px;
    }
  .image_titleen{
      margin-left: -40px;
    }
  .image_title{
    margin-top: 20px;
    width: 100%;
    span{
      margin-bottom: 10px;
    }
    span.span_right{
      width: 600px;
      margin-left: 65px;
      font-size: 14px;
      color: #606266;
    }
    .imagePath{
      width: 300px;
    }
    .imagePathen{
      margin-left: 10px;
    }
  }
  .careful{
      font-size: 14px;
      font-family: HarmonyHeiTi;
      font-weight: 300;
      color: #fff;
      margin-left: 130px !important;
      margin-bottom: 10px;
    }
   .careful.carefulen{
     margin-left: 151px !important;
   }
   .uploads{
     margin-left: 0;
   }
    .uploadBtn{
      margin-left: 65px;
      display: flex;
      .imageUpload{
          display: block;
          font-size: 16px !important;
          text-align: center;
          border-radius: 8px;
          font-family: HarmonyHeiTi !important;
          font-weight: 300 !important;
          color: #FFFFFF !important;
          background: #59508f !important;
          border-radius: 8px !important;
          padding: 6px 14px !important;
          margin-right: -90px !important;
          position: relative;
          left: -114px;
          top:-10px ;
          z-index: 10;
      }
    }
    .uploadBtnen{
      margin-top: 0 !important;
      margin-left: 0px  !important;
      width: 175px;
      line-height: 30px;
    }
  .uploader-example{
    width: 100%;
    .imageUploadTipDesc{
      font-size: 14px;
      color: #606266;
    }
    .uploader-listen{
      margin-left: 54px;
    }
  }
}
.el-icon-question{
  margin-left: 15px;
  font-size: 14px;
  color: #688ef3;
}
 .el-button {
  width: 174px !important;
  height: 46px !important;
  background-color: #fff !important;
  color: #380879 !important;
  border: none !important;
  border-radius: 12px !important;
  font-size: 20px !important;
}
.el-button:hover{
   background: #5E40C8 !important;
   color: #fff !important;
}
</style>
