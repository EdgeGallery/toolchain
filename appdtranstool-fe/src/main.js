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

import Vue from 'vue'
import App from './App.vue'
import router from './router.js'
import store from './store.js'
import ElementUI, { Notification } from 'element-ui'
import 'eg-view/src/assets/style/element-variables.scss'
import 'eg-view/src/assets/style/normal.css'
import './assets/style/normal.css'
import i18n from './locales/i18n.js'
import mavonEditor from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
import VueCookies from 'vue-cookies'
import uploader from 'vue-simple-uploader'
import VueAwesomeSwiper from 'vue-awesome-swiper'
import 'bootstrap/dist/css/bootstrap.min.css'
import Tooltip from 'vue-directive-tooltip'
Vue.use(VueAwesomeSwiper)
Vue.use(VueCookies)
Vue.use(mavonEditor)
Vue.use(ElementUI)
Vue.use(Tooltip)
Vue.use(uploader)
function isChromeBrowser () {
  let userAgent = navigator.userAgent
  if (userAgent.indexOf('Chrome') > -1) {
    return 'Chrome'
  }
}
if (isChromeBrowser() !== 'Chrome') {
  Notification.warning({
    title: 'warning',
    message: i18n.t('promptMessage.browserAdvise'),
    duration: 0,
    offset: 60
  })
}
new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
