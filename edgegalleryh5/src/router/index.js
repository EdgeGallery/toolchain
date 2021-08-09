import Vue from 'vue'
import Router from 'vue-router'
import Login from '../components/Login'
import Detil from '../components/Detil'
import Tourist from '../components/Tourist'
Vue.use(Router)
export default new Router({
  routes: [
    {
      path: '/',
      redirect: 'Login'
    },
    { path: '/Login', name:'/Login', component: Login },
    { path: '/Detil', name:'/Detil', component: Detil },
    { path: '/Tourist', name:'/Tourist', component:Tourist }
  ]
})
