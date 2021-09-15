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
    <p
      class="languageChange"
      @click="changeLanguage"
    >
      {{ getLanguage }}
    </p>
    <div
      class="appdTrans-content"
      v-if="hackReset"
    >
      <div class="stepNavProcess">
        <div class="appChangTop">
          <div class="topLeft">
            <img
              src="../../assets/images/icon.png"
              alt=""
            >
            <p v-show="active===0">
              {{ $t('appdRes.uploadApp') }}
            </p>
            <p v-show="active===1">
              {{ $t('appdRes.uploadAppImage') }}
            </p>
            <p v-show="active===2">
              {{ $t('appdRes.vmDeployConfig') }}
            </p>
          </div>
        </div>
        <div
          v-show="active===0"
          class="elSteps"
        >
          <uploadApp
            @getStepData="getStepData"
            @isChinaUnicomDest="isChinaUnicomDest"
            ref="uploadApp"
            :language="getLanguage"
          />
        </div>
        <div
          v-show="active===1"
          class="elSteps elStepsImage"
        >
          <uploadImage
            @getStepData="getStepData"
            ref="uploadImage"
            :language="getLanguage"
          />
        </div>
        <div
          v-show="active===2"
          class="elSteps elStepsDeloy"
          id="deploy"
        >
          <vmDeployConfig
            :language="getLanguage"
            @getStepData="getStepData"
            @child-event="parentEvent"
            @child-event2="parentEvent2"
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
  </div>
</template>

<script>
import finished from '@/assets/images/finished.png'
import notFinished from '@/assets/images/notFinished.png'
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
      finished: finished,
      notFinished: notFinished,
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
    parentEvent (data) {
      if (data === 2) {
        document.getElementById('deploy').style.height = '276px'
      }
    },
    parentEvent2 (data) {
      if (data === 1) {
        document.getElementById('deploy').style.height = '361px'
      }
    },
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
  width: 73.64%;
  margin: 0px auto;
  min-width: 1200px;
  min-height: 632px;
  background: #FFFFFF;
  padding: 8px 3.1% 0px 3.1%;
  p{
    margin: 0;
  }
  .languageChange{
    color: #000;
    font-size: 16px;
    line-height: 30px;
    float:right ;
  }
  .languageChange:hover{
    cursor: pointer;
    color: #5E40C8;
  }
  .appdTrans-content{
    width: 100%;
    background: #FFFFFF;
    padding-top: 1px;
    .stepNavProcess{
      margin-top: 30px;
      .appChangTop{
        height: 50px;
        display: flex;
        justify-content: space-between;
        .topLeft{
          width: 100%;
          margin-top: 10px;
          height: 40px;
          display: flex;
          border-radius:12px;
          background: linear-gradient(to right,#f8f6ff,#fff);
          img{
            width: 16px;
            height: 16px;
            margin: 12px 30px;
          }
          p{
            font-size: 16px;
            font-family: HarmonyHeiTi;
            font-weight: 400;
            color: #5E40C8;
            line-height: 40px;
          }
        }
      }
    }
    .elSteps {
      padding: 0 1%;
      width: 98%;
      box-sizing: border-box;
      background: #F1F2F6;
      border-radius: 16px;
      min-height: 380px;
      box-shadow: inset 4px 4px 25px 5px rgba(36, 20, 119, 0.1);
    }
    .elStepsImage{
      height: 200px;
    }
    .elStepsDeloy{
      height: 361px;
    }
    .elButton {
      width: 100%;
      margin-top: 60px;
      text-align: right;
    }
  }
}
.uploader-btn {
    font-size: 16px !important;
    font-family: HarmonyHeiTi  !important;
    font-weight: 300 !important;
    color: #FFFFFF !important;
    background: #59508f !important;
    border-radius: 8px !important;
    padding: 6px 14px !important;
    margin-right: 20px !important;
    margin-top: -10px !important;
}

.uploader-drop {
 padding-left:0px !important;
 background: #F1F2F6 !important;
}
.el-icon-question{
    display: none;
}
.imageUploadTipDesc{
  font-size: 14px ;
  font-family: HarmonyHeiTi  !important;
  font-weight: 300  !important;
  color: #380879  !important;
}
.el-form{
    background: #F1F2F6;
}
.el-form-item {
    margin-bottom: 6px !important;
}
.uploader-drop{
  border: none !important;
}
.el-form-item__label {
  font-size: 16px !important;
  font-family: HarmonyHeiTi !important;
  font-weight: 400 !important;
  color: #380879 !important;
}
.el-select {
    width: 600px;
}
.el-radio__label {
    color: #380879 !important;
}
.el-select > .el-input {
    border-radius: 8px;
}
.el-select .el-input .el-select__caret {
    color: #380879;
}
.el-input__inner{
  border: 1px solid #fff !important;
}
.el-radio__input.is-checked .el-radio__inner{
  background: url(../../assets/images/oneClick.png) 50% no-repeat !important;
  width: 19px;
  height: 19px;
}
.el-radio__input.is-disabled .el-radio__inner{
  background: url(../../assets/images/oneUnclick.png) 50% no-repeat !important;
  width: 19px;
  height: 19px;
}
.el-radio__inner::after {
  display: none;
}
</style>
