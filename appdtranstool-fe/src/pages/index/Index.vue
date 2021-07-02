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
  <div class="appdTrans">
    <div class="logoTitleLan">
      <div class="logo">
        <img
          src="../../assets/images/logo.png"
          class="curp"
          alt
        >
      </div>
      <div class="appdTransTittle">
        <h3>{{ $t('appdRes.appdTransTool') }}</h3>
        <span class="descDetail">{{ $t('appdRes.appdTransDes') }}</span>
      </div>
      <div class="languageChange">
        <span
          @click="changeLanguage"
          class="curp"
        >
          {{ getLanguage }}
        </span>
      </div>
    </div>

    <div
      class="appdTrans-content"
      v-if="hackReset"
    >
      <div class="stepNavProcess">
        <el-steps
          :active="active"
          finish-status="success"
          align-center
        >
          <el-step :title="$t('appdRes.uploadApp')" />
          <el-step :title="$t('appdRes.uploadAppImage')" />
          <el-step :title="$t('appdRes.vmDeployConfig')" />
        </el-steps>
      </div>
      <div
        v-show="active===0"
        class="elSteps"
      >
        <uploadApp
          @getStepData="getStepData"
          @isChinaUnicomDest="isChinaUnicomDest"
          ref="uploadApp"
        />
      </div>
      <div
        v-show="active===1"
        class="elSteps"
      >
        <uploadImage
          @getStepData="getStepData"
          ref="uploadImage"
        />
      </div>
      <div
        v-show="active===2"
        class="elSteps"
      >
        <vmDeployConfig
          @getStepData="getStepData"
          ref="vmDeployConfig"
        />
      </div>
      <div class="elButton">
        <el-button
          id="prevBtn"
          type="text"
          @click="previous"
          v-if="active>0"
          :disabled="isDeploying"
        >
          <strong>{{ $t('appdRes.preStep') }}</strong>
        </el-button>
        <el-button
          id="nextBtn"
          type="primary"
          @click="next"
          v-if="active<3"
        >
          <strong v-if="active!==2 && noChinaUnicomDest">{{ $t('appdRes.nextStep') }}</strong>
          <strong v-else>{{ $t('appdRes.submit') }}</strong>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>

import uploadApp from './UploadApp.vue'
import uploadImage from './UploadImage.vue'
import vmDeployConfig from './VmDeployConfig.vue'
import { appdTransAction } from '../../tools/api.js'
export default {
  name: 'Home',
  components: {
    uploadApp,
    uploadImage,
    vmDeployConfig
  },
  data () {
    return {
      active: 0,
      currentComponent: 'UploadApp',
      allStepData: {
        ifNext: false
      },
      sourceAppd: '',
      destAppd: '',
      appFile: '',
      docFile: '',
      imageFile: '',
      imagePath: '',
      az: '',
      flavor: '',
      bootData: '',
      deployFile: '',
      language: 'cn',
      hackReset: true,
      noChinaUnicomDest: true
    }
  },

  methods: {
    rebuileComponents () {
      this.hackReset = false
      this.$nextTick(() => {
        this.hackReset = true
      })
    },
    isChinaUnicomDest (flag) {
      if (flag) {
        this.noChinaUnicomDest = false
      } else {
        this.noChinaUnicomDest = true
      }
    },
    next () {
      if (this.active < 2) {
        if (!this.checkStep1()) {
          return
        }
        if (!this.checkStep2()) {
          return
        }
        if (this.active === 0) {
          this.$refs.uploadApp.parentMsg(this.active)
        }
        if (this.active === 1) {
          this.$refs.uploadImage.parentMsg(this.active)
        }
        if (this.checkChinaUnicomDest()) {
          this.submitTrans('ChinaUnicom')
          this.removeSessionStory()
          this.active = 0
          this.rebuileComponents()
          return
        }
        this.active++
      } else {
        this.$refs.vmDeployConfig.parentMsg(this.active)
        this.active = 0
        setTimeout(() => {
          this.submitTrans()
          this.removeSessionStory()
          this.rebuileComponents()
        }, 100)
      }
    },
    checkChinaUnicomDest () {
      if (this.active === 0) {
        let targetAppdType = JSON.parse(sessionStorage.getItem('targetAppdType'))
        if (targetAppdType === 'ChinaUnicom') {
          return true
        }
      }
      return false
    },
    checkStep1 () {
      if (this.active === 0) {
        let hasSourceAppd = JSON.parse(sessionStorage.getItem('hasSourceAppd'))
        let hasTargetAppd = JSON.parse(sessionStorage.getItem('hasTargetAppd'))
        let hasAppPackage = JSON.parse(sessionStorage.getItem('hasAppPackage'))
        if (!hasSourceAppd) {
          this.$message({
            duration: 2000,
            type: 'warning',
            message: this.$t('appdRes.sourceAppdNoEmpty')
          })
          return false
        } else if (!hasTargetAppd) {
          this.$message({
            duration: 2000,
            type: 'warning',
            message: this.$t('appdRes.targetAppdNoEmpty')
          })
          return false
        } else if (!hasAppPackage) {
          this.$message({
            duration: 2000,
            type: 'warning',
            message: this.$t('appdRes.appPackageNoEmpty')
          })
          return false
        }
      }
      return true
    },
    checkStep2 () {
      if (this.active === 1) {
        let imageUrl = JSON.parse(sessionStorage.getItem('isImageUrl'))
        let isImagePackage = JSON.parse(sessionStorage.getItem('isImagePackage'))
        if (!imageUrl && !isImagePackage) {
          this.$message({
            duration: 2000,
            message: this.$t('appdRes.chooseOneleast'),
            type: 'warning'
          })
          return false
        }
      }
      return true
    },
    removeSessionStory () {
      window.sessionStorage.removeItem('isImageUrl')
      window.sessionStorage.removeItem('isImagePackage')
      window.sessionStorage.removeItem('hasSourceAppd')
      window.sessionStorage.removeItem('hasTargetAppd')
      window.sessionStorage.removeItem('hasAppPackage')
    },

    previous () {
      this.active--
    },
    getStepData (data) {
      this.allStepData[data.step] = data
    },
    submitTrans (type) {
      let param = {}
      if (type) {
        param = {
          sourceAppd: this.allStepData.step1.sourceAppd,
          destAppd: this.allStepData.step1.destAppd,
          appFile: this.allStepData.step1.appFile.data,
          docFile: this.allStepData.step1.docFile.data,
          imageFile: null,
          imagePath: '',
          az: '',
          flavor: '',
          bootData: '',
          deployFile: null
        }
      } else {
        param = {
          sourceAppd: this.allStepData.step1.sourceAppd,
          destAppd: this.allStepData.step1.destAppd,
          appFile: this.allStepData.step1.appFile.data,
          docFile: this.allStepData.step1.docFile.data,
          imageFile: this.allStepData.step2.imageFile.data,
          imagePath: this.allStepData.step2.imageAddr,
          az: this.allStepData.step3.az,
          flavor: this.allStepData.step3.flavor,
          bootData: this.allStepData.step3.bootData,
          deployFile: this.allStepData.step3.deployFile.data
        }
      }
      appdTransAction(param).then((res) => {
        let blob = res.data
        let filename = res.headers['content-disposition']
        if (filename) {
          let index = filename.indexOf('filename=')
          filename = filename.substring(index + 9)
          let suffix = param.appFile.split('.')[1]
          filename = decodeURI(filename) + '.' + suffix
        }
        let objectUrl = URL.createObjectURL(blob)
        let a = document.createElement('a')
        a.href = objectUrl
        a.download = filename
        a.click()
        this.$message({
          duration: 2000,
          message: this.$t('appdRes.transformSuccess'),
          type: 'success'
        })
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
    changeLanguage () {
      if (this.language === 'cn') {
        this.language = 'en'
      } else {
        this.language = 'cn'
      }
      localStorage.setItem('language', this.language)
      this.$i18n.locale = this.language
      this.$store.commit('changeLaguage', { language: this.language })
    }
  },
  computed: {
    getLanguage () {
      let language
      this.language === 'cn' ? language = 'English' : language = '中文'
      return language
    }
  }
}
</script>

<style lang="less">
.appdTrans{
  width: 70%;
  height: 100%;
  background: #FFFFFF;
  margin: 50px auto;
  padding-top: 15px;
  .logoTitleLan{
    text-align: center;
    .logo {
      height: 65px;
      line-height: 65px;
      margin-left: 15px;
      float: left;
      display: inline-block;
      img {
        height: 65px;
      }
      span {
        font-size: 18px;
        vertical-align: text-bottom;
      }
    }
    .appdTransTittle{
      display: inline-block;
      margin: 0px 80px 40px 0px;
      .descDetail{
        color: #B4B7BF;
        width: 800px !important;
        float: left !important;
        overflow: hidden !important;
        text-overflow: ellipsis !important;
        white-space: normal !important;
      }
    }
    .languageChange{
      display: inline-block;
      float: right;
      margin-right: 15px;
    }
  }
  .appdTrans-content{
    width: 100%;
    height: 90%;
    background: #FFFFFF;
    padding-top: 1px;
    .stepNavProcess{
      margin-top: 20px;
    }
    .elSteps {
      margin: 0px 10% 0;
      width: 80%;
      padding: 20px 0;
      box-sizing: border-box;
      border-bottom:  1px dashed #dfe1e6;
    }
    .elButton {
      width: 80%;
      margin: 30px 10% 25px;
      text-align: right;
      button {
        height: 30px;
        width: 110px;
        line-height: 30px;
        padding: 0;
      }
      .el-button--text {
        border: 1px solid #688ef3;
      }
      .el-button--primary {
        background-color: #688ef3;
        color: #fff;
      }
    }
  }
}
</style>
