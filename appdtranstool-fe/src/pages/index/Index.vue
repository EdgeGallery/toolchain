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
    <div
      class="appdTrans-content"
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
      <div class="elSteps">
        <component
          :is="currentComponent"
          :active="active"
          @getStepData="getStepData"
          :all-step-data="allStepData"
          ref="currentComponet"
        />
      </div>
      <div class="elButton">
        <el-button
          id="prevBtn"
          type="text"
          @click="previous"
          v-if="active>1"
          :disabled="isDeploying"
        >
          <strong>{{ $t('appdRes.preStep') }}</strong>
        </el-button>
        <el-button
          id="nextBtn"
          type="primary"
          @click="next"
          v-if="active<4"
        >
          <strong v-if="active!==3">{{ $t('appdRes.nextStep') }}</strong>
          <strong v-else>{{ $t('appdRes.submit') }}</strong>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>

import UploadApp from './UploadApp.vue'
import UploadImage from './UploadImage.vue'
import VmDeployConfig from './VmDeployConfig.vue'
import { appdTransAction } from '../../tools/api.js'
export default {
  name: 'Home',
  components: {
    UploadApp,
    UploadImage,
    VmDeployConfig
  },
  data () {
    return {
      active: 1,
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
      deployFile: ''
    }
  },

  methods: {
    changeComponent () {
      switch (this.active) {
        case 1:
          this.currentComponent = 'UploadApp'
          break
        case 2:
          this.currentComponent = 'UploadImage'
          break
        case 3:
          this.currentComponent = 'VmDeployConfig'
          break
        default:
          this.currentComponent = 'UploadApp'
      }
    },
    next () {
      if (this.active < 3) {
        this.active++
        this.handleStep()
      } else {
        this.active = 1
        this.handleStep()
        setTimeout(() => {
          this.submitTrans()
        }, 1000)
      }
    },

    previous () {
      this.active--
      this.handleStep()
    },
    handleStep () {
      this.changeComponent()
    },
    getStepData (data) {
      this.allStepData[data.step] = data
    },
    submitTrans () {
      let param = {
        sourceAppd: this.allStepData.step1.sourceAppd,
        destAppd: this.allStepData.step1.destAppd,
        appFile: this.allStepData.step1.appFile.data,
        docFile: this.allStepData.step1.docFile.data,
        imageFile: this.allStepData.step2.imageFile.data,
        imagePath: '',
        az: this.allStepData.step3.az,
        flavor: this.allStepData.step3.flavor,
        bootData: this.allStepData.step3.bootData,
        deployFile: ''
      }
      appdTransAction(param).then((res) => {
        let blob = res.data
        let objectUrl = URL.createObjectURL(blob)
        let a = document.createElement('a')
        a.href = objectUrl
        a.download = 'transFile.zip'
        a.click()
        this.$message({
          duration: 2000,
          message: this.$t('appdRes.errorCode0'),
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
    }
  },
  computed: {
  },
  watch: {
  },
  mounted () {
  }
}
</script>

<style lang="less">
.appdTrans{
  width: 70%;
  height: 100%;
  background: #FFFFFF;
  margin: 150px auto;
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
