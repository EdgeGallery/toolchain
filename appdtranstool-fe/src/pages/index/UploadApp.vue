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
  <div class="UploadApp">
    <el-form
      v-model="appUploadInfo"
      ref="form"
      label-width="200px"
      :rules="rules"
    >
      <el-form-item
        :label="$t('appdRes.appDpMode')"
      >
        <el-radio
          class="radioStyle"
          v-model="radio"
          label="1"
        >
          {{ $t('appdRes.vm') }}
        </el-radio>
        <el-radio
          class="radioStyle"
          v-model="radio"
          label="2"
          :disabled="true"
        >
          {{ $t('appdRes.container') }}
        </el-radio>
      </el-form-item>
      <el-form-item
        :label="$t('appdRes.sourceAppdStandard')"
        prop="sourceAppStandard"
      >
        <el-select
          v-model="appUploadInfo.sourceAppd"
          clearable
          @change="changSelect(appUploadInfo.sourceAppd)"
        >
          <el-option
            v-for="(item,index) in appdStandardTypes"
            :key="index"
            :label="item.label"
            :value="item.value"
            :disabled="item.disabled"
            @click.native="getSelectSourceAppd(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="$t('appdRes.targetAppdStandard')"
        prop="targetAppStandard"
      >
        <el-select
          v-model="appUploadInfo.destAppd"
          clearable
          @change="changSelect(appUploadInfo.destAppd)"
        >
          <el-option
            v-for="(item,index) in appdStandardTypes"
            :key="index"
            :label="item.label"
            :value="item.value"
            :disabled="item.disabled"
            @click.native="getSelectTargetAppd(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="$t('appdRes.appPackage')"
        prop="appPackage"
      >
        <uploader
          :options="options"
          class="uploader-example"
          @file-complete="fileComplete"
          @file-added="onFileAdded"
          :limit="1"
        >
          <uploader-unsupport />
          <uploader-drop>
            <uploader-btn
              :single="true"
            >
              {{ $t('appdRes.uploadAppPackage') }}
            </uploader-btn>
            <em class="el-icon-question" />
            <span class="imageUploadTipDesc">{{ $t('appdRes.uploadPackageTip') }}</span>
          </uploader-drop>
          <uploader-list />
        </uploader>
      </el-form-item>
      <el-form-item
        :label="$t('appdRes.appDescription')"
        v-show="appUploadInfo.destAppd==='ChinaUnicom'?false:true"
      >
        <uploader
          :options="options"
          class="uploader-example"
          @file-complete="fileComplete"
          @file-added="onMdFileAdded"
          :limit="1"
        >
          <uploader-unsupport />
          <uploader-drop>
            <uploader-btn>
              {{ $t('appdRes.uploadAppDescription') }}
            </uploader-btn>
            <em class="el-icon-question" />
            <span class="imageUploadTipDesc">{{ $t('appdRes.uploadDescriptionTip') }}</span>
          </uploader-drop>
          <uploader-list />
        </uploader>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getTemplates } from '../../tools/api.js'
import axios from 'axios'
import { getCookie } from '../../tools/request.js'
export default {
  data () {
    return {
      appdStandardTypes: [],
      appUploadInfo: {
        step: 'step1',
        sourceAppd: '',
        destAppd: '',
        appFile: '',
        docFile: ''
      },
      radio: '1',
      language: 'cn',
      options: {
        testChunks: false,
        headers: {},
        forceChunkSize: true,
        simultaneousUploads: 5,
        chunkSize: 8 * 1024 * 1024
      },
      mergerUrl: '',
      uploadFileType: '',
      rules: {
        sourceAppStandard: [
          { required: true, message: '源APPD标准不能为空', trigger: 'blur' }
        ],
        targetAppStandard: [
          { required: true, message: '目标APPD标准不能为空', trigger: 'blur' }
        ],
        appPackage: [
          { required: true, message: '请上传应用包', trigger: 'blur' }
        ]
      }
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
    getSelectSourceAppd (item) {
      sessionStorage.setItem('hasSourceAppd', JSON.stringify(true))
    },
    getSelectTargetAppd (item) {
      sessionStorage.setItem('hasTargetAppd', JSON.stringify(true))
      sessionStorage.setItem('targetAppdType', JSON.stringify(item.value))
    },
    getTemplates () {
      getTemplates().then((res) => {
        for (let item of res.data) {
          console.log(item)
          let selectItem = {
            label: item,
            value: item,
            disabled: false
          }
          this.appdStandardTypes.push(selectItem)
        }
        console.log('get templates success')
      }).catch(() => {
        this.$message({
          duration: 2000,
          message: this.$t('appdRes.uploadApp'),
          type: 'warning'
        })
      })
    },
    getFileType (name) {
      let fileSuffix = name.split('.')[1]
      if (fileSuffix === 'csar' || fileSuffix === 'zip') {
        this.uploadFileType = 'package'
      } else {
        this.uploadFileType = 'doc'
      }
    },
    onFileAdded (file) {
      let fileSize = file.file.size / 1024 / 1024 / 1024
      let typeName = file.file.name.substring(file.file.name.lastIndexOf('.') + 1)
      let typeArr = ['csar', 'zip']
      if (typeArr.indexOf(typeName) === -1 || fileSize > 5) {
        file.ignored = true
        this.$message.warning(this.$t('appdRes.uploadPackageTip'))
      }
    },
    onMdFileAdded (file) {
      let fileSize = file.file.size / 1024 / 1024 / 1024
      let typeName = file.file.name.substring(file.file.name.lastIndexOf('.') + 1)
      let typeArr = ['md', 'MD']
      if (typeArr.indexOf(typeName) === -1 || fileSize > 5) {
        file.ignored = true
        this.$message.warning(this.$t('appdRes.uploadMdTip'))
      }
    },
    fileComplete (fileType) {
      const file = arguments[0].file
      this.getFileType(file.name)
      let url = this.mergerUrl + file.name + '&guid=' + arguments[0].uniqueIdentifier + '&fileType=' + this.uploadFileType
      axios.get(url).then(response => {
        if (this.uploadFileType === 'package') {
          this.appUploadInfo.appFile = response.data
          sessionStorage.setItem('hasAppPackage', JSON.stringify(true))
        } else {
          this.appUploadInfo.docFile = response.data
        }
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
    changSelect (value) {
      let allSelect = [this.appUploadInfo.sourceAppd, this.appUploadInfo.destAppd]
      this.appdStandardTypes.forEach((ele) => {
        ele.disabled = false
      })
      this.appdStandardTypes.forEach((ele) => {
        allSelect.forEach((element) => {
          if (element === ele.value) {
            ele.disabled = true
          }
        })
      })
    },
    parentMsg: function (active) {
      if (active === 1) {
        this.$emit('getStepData', this.appUploadInfo)
      }
    }
  },
  mounted () {
    this.getTemplates()
  }
}
</script>

<style lang="less">
.UploadApp{
  width: 80%;
  margin-left: 8%;
  margin-top: 5%;
  background: #FFFFFF;
  .radioStyle{
    margin-top: 12px;
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
