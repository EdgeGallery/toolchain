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
  <div class="VmDeployConfig">
    <div
      class="look"
      @click="click1"
    >
      <img
        :src="activeTabIndex === 1 ? oneClick : oneUnclick"
        class="clicks"
        alt=""
      >
      <p class="title">
        {{ $t('appdRes.showConfig') }}
      </p>
    </div>
    <div
      v-if="activeTabIndex===1"
      class="showConfigDiv"
    >
      <el-form
        class="formConfing"
        label-position="left"
        v-model="configInfo"
        ref="form"
        label-width="110px"
      >
        <el-form-item
          label="AZ"
        >
          <el-input
            id="az"
            v-model="configInfo.az"
          />
        </el-form-item>
        <el-form-item
          label="flavor"
        >
          <el-input
            id="flavor"
            v-model="configInfo.flavor"
          />
        </el-form-item>
        <el-form-item
          label="bootData"
        >
          <el-input
            id="bootData"
            v-model="configInfo.bootData"
          />
        </el-form-item>
      </el-form>
    </div>
    <div
      class="look look2"
      @click="click2"
    >
      <img
        :src="activeTabIndex === 1 ? oneUnclick : oneClick"
        class="clicks"
        alt=""
      >
      <p class="title">
        {{ $t('appdRes.editOnLine') }}
      </p>
    </div>
    <div v-if="activeTabIndex === 2">
      <uploader
        :options="options"
        class="uploader-example"
        @file-complete="fileComplete"
        @file-added="onFileAdded"
        accept=".zip"
      >
        <uploader-unsupport />
        <span class="imageUploadTipDesc UploadTipDesc">{{ $t('appdRes.upoadYaml') }}</span>
        <uploader-drop>
          <uploader-btn>{{ $t('appdRes.templateUpload') }}</uploader-btn>
          <a
            :href="currentTemplate"
            download="demo.yaml"
            class="down-demo"
          >
            {{ $t('appdRes.downloadTemplate') }}
          </a>
        </uploader-drop>
        <uploader-list />
      </uploader>
      <div v-show="hasValidate">
        <div :class="appYamlFileId ? 'green test tit' : 'red test tit'">
          {{ appYamlFileId ? $t('appdRes.uploadFile') : $t('appdRes.uploadFile') }}
        </div>
        <div
          class="test tit"
          v-show="showResult"
        >
          <div :class="checkFlag.formatSuccess ? 'green test' : 'red test'">
            <em
              v-show="checkFlag.formatSuccess"
              class="el-icon-circle-check"
            />
            <em
              v-show="!checkFlag.formatSuccess"
              class="el-icon-circle-close"
            />
            {{ $t('appdRes.uploadFile') }}
          </div>
          <div :class="checkFlag.imageSuccess ? 'green test' : 'red test'">
            <em
              v-show="checkFlag.imageSuccess"
              class="el-icon-circle-check"
            />
            <em
              v-show="!checkFlag.imageSuccess"
              class="el-icon-circle-close"
            />
            {{ $t('appdRes.uploadFile') }}
          </div>
          <div :class="checkFlag.serviceSuccess ? 'green test' : 'red test'">
            <em
              v-show="checkFlag.serviceSuccess"
              class="el-icon-circle-check"
            />
            <em
              v-show="!checkFlag.serviceSuccess"
              class="el-icon-circle-close"
            />
            {{ $t('appdRes.uploadFile') }}
          </div>
          <div :class="checkFlag.mepAgentSuccess ? 'green test' : 'yellow test'">
            <em
              v-show="checkFlag.mepAgentSuccess"
              class="el-icon-circle-check"
            />
            <em
              v-show="!checkFlag.mepAgentSuccess"
              class="el-icon-warning-outline"
            />
            {{ $t('appdRes.uploadFile') }}
          </div>
        </div>
      </div>
      <div
        class="yaml_content"
        v-if="fileUploadSuccess"
      >
        <mavon-editor
          v-model="markdownSource"
          :toolbars-flag="false"
          :editable="false"
          :subfield="false"
          default-open="preview"
          :box-shadow="false"
          preview-background="#ffffff"
        />
      </div>
    </div>
  </div>
</template>

<script>
import oneClick from '@/assets/images/oneClick.png'
import oneUnclick from '@/assets/images/oneUnclick.png'
import chinaunicomTemplate from '@/assets/file/chinaunicomTemplate.yaml'
import edgegalleryTemplate from '@/assets/file/edgegalleryTemplate.yaml'
import axios from 'axios'
import { getCookie } from '../../tools/request.js'
export default {
  components: {
  },
  data () {
    return {
      activeTabIndex: 1,
      oneClick: oneClick,
      oneUnclick: oneUnclick,
      configInfo: {
        step: 'step3',
        az: '',
        flavor: '',
        bootData: '',
        deployFile: ''
      },
      activeName: 'first',
      yamlFileList: [],
      uploadYamlLoading: false,
      currentTemplate: chinaunicomTemplate,
      showResult: true,
      checkFlag: {},
      options: {
        testChunks: false,
        headers: {},
        forceChunkSize: true,
        simultaneousUploads: 5,
        chunkSize: 8 * 1024 * 1024,
        singleFile: true
      },
      mergerUrl: '',
      fileAddress: ''
    }
  },
  created () {
    this.options.headers = { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') }
    let url = window.location.origin
    this.options.target = url + '/mec/appdtranstool/v1/vm/upload'
    this.mergerUrl = url + '/mec/appdtranstool/v1/vm/apps/merge?fileName='
  },
  methods: {

    click1 () {
      this.activeTabIndex = 1
      this.$emit('child-event2', this.activeTabIndex)
    },
    click2 () {
      this.activeTabIndex = 2
      this.$emit('child-event', this.activeTabIndex)
    },
    parentMsg: function (active) {
      if (active === 2) {
        this.$emit('getStepData', this.configInfo)
      }
    },
    fileComplete (fileType) {
      const file = arguments[0].file
      let url = this.mergerUrl + file.name + '&guid=' + arguments[0].uniqueIdentifier + '&fileType=deploy'
      axios.get(url).then(response => {
        this.configInfo.deployFile = response.data
      }).catch(error => {
        console.log(error)
      })
    },
    onFileAdded (file) {
      let fileSize = file.file.size / 1024 / 1024 / 1024
      let typeName = file.file.name.substring(file.file.name.lastIndexOf('.') + 1)
      let typeArr = ['yaml', 'YAML']
      if (typeArr.indexOf(typeName) === -1 || fileSize > 5) {
        file.ignored = true
        this.$message.warning(this.$t('appdRes.uploadYamlTip'))
      }
    },
    handleClick (tab, event) {
      this.activeTabIndex = tab.index
    }
  },
  mounted () {
    let targetAppdType = JSON.parse(sessionStorage.getItem('targetAppdType'))
    if (targetAppdType === 'ChinaUnicom') {
      this.currentTemplate = chinaunicomTemplate
    } else if (targetAppdType === 'EdgeGallery') {
      this.currentTemplate = edgegalleryTemplate
    }
  }
}
</script>

<style lang="less">
.VmDeployConfig{
  width: 100%;
  margin-top: 40px;
  padding-top: 30px;
  padding-left:112px;
  .uploader-drop {
  margin-left: 60px;
}
  .look2{
    margin-top: 16px;
  }
  .look:hover{
    cursor: pointer;
  }
  .look{
    width: 900px;
    height: 47px;
    display: flex;
    justify-content: flex-start;
    border-radius: 19.5px;
    background: #4E3494;
    box-shadow: 0px 7px 21px 0px rgba(40, 12, 128, 0.08);
    .clicks{
      margin: 14px 0 0 32px;
      width: 19px;
      height: 19px;
    }
    .title{
      margin-left:16px;
      line-height: 47px;
      font-size: 18px;
      font-family: defaultFontLight,Arial,Helvetica,sans-serif!important;
      font-weight: 400;
      color: #fff;
    }
  }

  .showConfigDiv{
    width: 100%;
    .formConfing{
      text-align: left;
    }
  }
  .upload-demo{
    .el-upload{
      float: left;
    }
    .el-upload__tip{
      font-size: 16px;
      float: left;
    }
    .el-upload-list{
      float: left;
      width: 100%;
    }
  }
  .down-demo {
    text-decoration: none;
    display: inline-block;
    font-size: 16px !important;
    font-family: defaultFontLight,Arial,Helvetica,sans-serif!important;
    font-weight: 300 !important;
    color: #fff !important;
    background: #4E3494 !important;
    border-radius: 8px !important;
    padding: 6px 20px !important;
    margin-right: 20px !important;
    margin-top: -10px !important;
  }
  .down-demo:hover{
    color: #4E3494 !important;
    background: #fff !important;
    cursor: pointer;
  }
  .test {
    font-size: 14px;
    margin-top: 15px;
    margin-right: 15px;
    display: inline-block;
  }
  .test.tit{
    display: block;
  }
  .green {
    color: green
  }
  .red {
    color: red
  }
  .yellow{
    color:#dbb419;
  }
  .yaml_content{
    line-height: 25px;
    white-space: pre-wrap;
    margin-top: 20px;
    overflow: auto;
    min-height: 600px;
    .v-note-wrapper{
      border: none;
    }
    .v-note-wrapper .v-note-panel .v-note-show{
      overflow: hidden;
      .hljs, pre{
        background: #1e1e1e;
        color: #fff;
      }
    }
  }
}
.UploadTipDesc{
  display: block;
  font-size: 16px !important;
  font-family: defaultFontLight,Arial,Helvetica,sans-serif!important;
  font-weight: 300;
  color: #380879;
  margin: 20px 0 10px 66px;
}
.el-input__inner {
    width: 600px;
}
.el-form-item {
  margin-top: 16px;
}

</style>
