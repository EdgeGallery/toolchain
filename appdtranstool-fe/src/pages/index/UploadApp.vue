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
          @change="changSelect(appUploadInfo.sourceAppd, 0)"
          @clear="clearSourceAppd"
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
        <p class="lookDoc">
          {{ '('+ $t('appdRes.appdTransDes') }}
          <a :href="this.language ==='中文'?'https://gitee.com/edgegallery/toolchain/blob/master/appdtranstool/README_en.md':'https://gitee.com/edgegallery/toolchain/blob/master/appdtranstool/README.md'">
            {{ $t('appdRes.appdDocuments') }}
          </a>
          )
        </p>
      </el-form-item>
      <el-form-item
        :label="$t('appdRes.targetAppdStandard')"
        prop="targetAppStandard"
      >
        <el-select
          v-model="appUploadInfo.destAppd"
          clearable
          @change="changSelect(appUploadInfo.destAppd, 1)"
          @clear="clearDestAppd"
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
  props: {
    language: {
      type: String,
      required: true
    }
  },
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
      options: {
        testChunks: false,
        headers: {},
        forceChunkSize: true,
        simultaneousUploads: 5,
        chunkSize: 8 * 1024 * 1024,
        singleFile: true
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
    this.options.target = url + '/mec/appdtranstool/v1/vm/upload'
    this.mergerUrl = url + '/mec/appdtranstool/v1/vm/apps/merge?fileName='
  },
  methods: {
    getSelectSourceAppd (item) {
      sessionStorage.setItem('hasSourceAppd', JSON.stringify(true))
    },
    clearSourceAppd () {
      sessionStorage.removeItem('hasSourceAppd')
    },
    clearDestAppd () {
      sessionStorage.removeItem('hasTargetAppd')
    },
    getSelectTargetAppd (item) {
      sessionStorage.setItem('hasTargetAppd', JSON.stringify(true))
      sessionStorage.setItem('targetAppdType', JSON.stringify(item.value))
    },
    getTemplates () {
      getTemplates().then((res) => {
        for (let item of res.data) {
          let selectItem = {
            label: item,
            value: item,
            disabled: false
          }
          this.appdStandardTypes.push(selectItem)
        }
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
      console.log(fileType)
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
    changSelect (value, selectType) {
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
      if (selectType === 1) {
        if (value === 'ChinaUnicom') {
          this.$emit('isChinaUnicomDest', true)
        } else {
          this.$emit('isChinaUnicomDest', false)
        }
      }
    },
    parentMsg: function (active) {
      if (active === 0) {
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
  width: 100%;
  margin-top: 40px;
  padding: 40px 0 30px 0px;
  .radioStyle{
    margin-top: 12px;
  }
  .lookDoc{
            margin-top: 6px;
            font-size: 12px;
            font-family: HarmonyHeiTi;
            font-weight: 200;
            color: #5E40C8;
            line-height: 20px;
            a{
            font-size: 12px;
            font-family: HarmonyHeiTi;
            font-weight: 200;
            font-weight: bold;
            color: #5E40C8;
            line-height: 20px;
            }
  }
}

</style>
