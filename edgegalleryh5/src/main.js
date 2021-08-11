// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
Vue.prototype.$axios = axios
Vue.config.productionTip = false
axios.defaults.headers.common['Authorization'] = 'Basic ZWxhc3RpYzoxMjM0NTY='
axios.defaults.headers.post['Content-Type'] = 'application/json'
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  axios,
  components: { App },
  template: '<App/>'
})
