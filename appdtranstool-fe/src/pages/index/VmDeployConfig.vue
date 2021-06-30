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
    <el-tabs
      v-model="activeName"
      @tab-click="handleClick"
    >
      <el-tab-pane
        :label="$t('appdRes.showConfig')"
        name="first"
      >
        <div
          v-if="activeTabIndex==='0'"
          class="showConfigDiv"
        >
          <el-form
            class="formConfing"
            label-position="left"
            v-model="configInfo"
            ref="form"
            label-width="150px"
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
      </el-tab-pane>
      <el-tab-pane
        :label="$t('appdRes.editOnLine')"
        name="second"
      >
        <uploader
          :options="options"
          class="uploader-example"
          @file-complete="fileComplete"
          @file-added="onFileAdded"
          accept=".zip"
        >
          <uploader-unsupport />
          <uploader-drop>
            <uploader-btn>{{ $t('appdRes.templateUpload') }}</uploader-btn>
            <em class="el-icon-question" />
            <span class="imageUploadTipDesc">{{ $t('appdRes.upoadYaml') }}</span>
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import chinaunicomTemplate from '@/assets/file/chinaunicomTemplate.yaml'
import edgegalleryTemplate from '@/assets/file/edgegalleryTemplate.yaml'
import axios from 'axios'
import { getCookie } from '../../tools/request.js'
export default {
  components: {
  },
  data () {
    return {
      activeTabIndex: '0',
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
        chunkSize: 8 * 1024 * 1024
      },
      mergerUrl: '',
      fileAddress: ''
    }
  },
  created () {
    this.options.headers = { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN') }
    let url = window.location.origin
    url = url.replace('8083', '9082')
    this.options.target = url + '/mec/appdtranstool/v1/vm/upload'
    this.mergerUrl = url + '/mec/appdtranstool/v1/vm/apps/merge?fileName='
  },
  methods: {
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
    },
    handleExceed (file, fileList) {
      if (fileList.length === 1) {
        this.$message.warning(this.$t('appdRes.uploadFile'))
      }
    },
    handleChangeYaml (file, fileList) {
      let yamlFileList = []
      yamlFileList.push(file.raw)
      this.yamlFileList = []
      const fileType = file.raw.name.substring(file.raw.name.lastIndexOf('.') + 1)
      const fileTypeArr = ['yaml']
      if (!fileTypeArr.includes(fileType)) {
        this.$message.warning(this.$t('appdRes.uploadFile'))
        yamlFileList = []
      }
      if (yamlFileList.length > 0) {
        this.submitYamlFile(yamlFileList)
      }
    },
    removeUploadyaml (file, fileList) {
    },
    submitYamlFile (yamlFileList) {
      this.uploadYamlLoading = true
      let fd = new FormData()
      fd.append('file', yamlFileList[0])
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
  .el-tabs{
    .el-tabs__item{
      height: 15px;
      line-height: 15px;
      padding: 0 20px;
      font-size: 18px;
      margin:10px 0 18px 0;
      border-right: 1px solid #ddd;
      border-radius: 0;
    }
    .el-tabs__item:last-child{
      border-right: 0;
    }
    .el-tabs__item.is-active{
      color: #688ef3;
    }
    .el-tabs__item.is-disabled{
      cursor: not-allowed;
    }
    .el-tabs__active-bar{
      height: 4px;
    }
    .el-tab-pane{
      padding: 20px 0;
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
      display: inline-block;
      margin: 5px 0 0 15px;
      font-size: 16px;
      cursor: pointer;
      color: #688ef3;
    }
  }
  width: 80%;
  margin-left: 8%;
  margin-top: 5%;
  background: #FFFFFF;
  .showConfigDiv{
    width: 100%;
    height: 200px;
    background: #FFFFFF;
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
    display: inline-block;
    margin: 5px 0 0 15px;
    font-size: 16px;
    cursor: pointer;
    color: #688ef3;
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
.el-icon-question{
  margin-left: 15px;
  font-size: 14px;
  color: #688ef3;
}
</style>
