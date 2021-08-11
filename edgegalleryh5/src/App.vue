<template>
  <div id="app" v-show="look">
    <router-view></router-view>
  </div>
</template>
<script>
import Detil from '../src/components/Detil'
export default {
  name: 'App',
  components:{
    Detil
  },
  data(){
    return {
      look:true
    }
  }
}
</script>

<style>
html, body, #app{
  height: 100%;
}
html{
  font-size: 12px;
}
*{
  margin: 0;
  padding: 0;
}
</style>
