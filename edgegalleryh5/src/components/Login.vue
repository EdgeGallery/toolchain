<template>
   <div class='login'>
        <img src='../assets/login.jpg' alt class="persent" />
        <div class="user_text">
            <input type='text'  v-model="useName" class='input' placeholder='请输入您的账户生成您的专属记忆' />
        </div>
        <div class="btn">
            <img src="../assets/login_btn.png" alt="" @click='submit()'>
        </div>
   </div>
</template>
<script>
import { getDataApi, getPr, getComments, getIssue, getDemoNum, getMeet, getStar, getFork, getTop } from '../api/wxapi'
export default {
  name: 'login',
  data () {
    return {
      useName:'',
      user1: '',
      firstLightHouse:'',
      addLightHouse:'',
      firstPr:'',
      addPr:'',
      firstComments:'',
      addComments:'',
      firstIssue:'',
      addIssue:'',
      demoNum:'',
      firstMeet:'',
      date:'',
      addDays:'',
      firstStar:'',
      addStar:'',
      firstFork:'',
      addFork:'',
      tops:{},
      placing:'',
      url:'',
      get1:[],
      get2:'',
      get3:[],
      get4:'',
      get5:[],
      get6:'',
      get7:[],
      get8:'',
      get9:'',
      get10:[],
      get11:[],
      get12:'',
      get13:[],
      get14:'',
      get15:''
    }
  },
  created(){

  },
  methods: {
    submit() {
      this.tourist()
      var myreg = /^[a-zA-Z][0-9a-zA-Z_-]{1,34}$/
      var getUser = new Promise((resolve, reject) => {
        getDataApi(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstLightHouse = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            this.firstLightHouse = this.firstLightHouse.substr(0, 4) + '年' + this.firstLightHouse.substr(5, 2) + '月' + this.firstLightHouse.substr(8, 2) + '日'
          }
          this.addLightHouse = res.data.hits.total.value
          this.get2 = res.data.hits.total.value
          this.get1 = res.data.hits.hits
        })
      })
      var getUserPr = new Promise((resolve, reject) => {
        getPr(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstPr = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            this.firstPr = this.firstPr.substr(0, 4) + '年' + this.firstPr.substr(5, 2) + '月' + this.firstPr.substr(8, 2) + '日'
          }
          this.get3 = res.data.hits.hits
          this.addPr = res.data.hits.total.value
          this.get4 = res.data.hits.total.value
        })
      })
      var getUserComments = new Promise((resolve, reject) => {
        getComments(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstComments = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            this.firstComments = this.firstComments.substr(0, 4) + '年' + this.firstComments.substr(5, 2) + '月' + this.firstComments.substr(8, 2) + '日'
          }
          this.addComments = res.data.hits.total.value
          this.get5 = res.data.hits.hits
          this.get6 = res.data.hits.total.value
        })
      })
      var getUserIssue = new Promise((resolve, reject) => {
        getIssue(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstIssue = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            this.firstIssue = this.firstIssue.substr(0, 4) + '年' + this.firstIssue.substr(5, 2) + '月' + this.firstIssue.substr(8, 2) + '日'
          }
          this.addIssue = res.data.hits.total.value
          this.get7 = res.data.hits.hits
          this.get8 = res.data.hits.total.value
        })
      })
      var getUserDemoNum = new Promise((resolve, reject) => {
        getDemoNum(this.useName).then(res => {
          resolve(res)
          this.demoNum = res.data.aggregations.sum.value
          this.get9 = res.data.aggregations.sum.value
        })
      })
      var getUserMeet = new Promise((resolve, reject) => {
        getMeet(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstMeet = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            this.firstMeet = this.firstMeet.substr(0, 4) + '年' + this.firstMeet.substr(5, 2) + '月' + this.firstMeet.substr(8, 2) + '日'
            this.date = res.data.hits.hits[0]._source.created_at.substr(0, 10)
            let date2 = new Date()
            let date1 = new Date(Date.parse(this.date.replace(/-/g, '/')))
            this.addDays = parseInt(Math.abs(date2.getTime() - date1.getTime()) / 1000 / 60 / 60 / 24)
          }
          this.get10 = res.data.hits.hits
        })
      })
      var getUserStar = new Promise((resolve, reject) => {
        getStar(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstStar = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          }
          this.firstStar = this.firstStar.substr(0, 4) + '年' + this.firstStar.substr(5, 2) + '月' + this.firstStar.substr(8, 2) + '日'
          this.addStar = res.data.hits.total.value
          this.get11 = res.data.hits.hits
          this.get12 = res.data.hits.total.value
        })
      })
      var getUserFork = new Promise((resolve, reject) => {
        getFork(this.useName).then(res => {
          resolve(res)
          if (res.data.hits.hits.length !== 0){
            this.firstFork = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          }
          this.firstFork = this.firstFork.substr(0, 4) + '年' + this.firstFork.substr(5, 2) + '月' + this.firstFork.substr(8, 2) + '日'
          this.addFork = res.data.hits.total.value
          this.get13 = res.data.hits.hits
          this.get14 = res.data.hits.total.value
        })
      })
      var getUserTop = new Promise((resolve, reject) => {
        getTop(this.useName).then(res => {
          resolve(res)
          this.tops = res.data.aggregations.uniq_gender.buckets
          let totals = Object.keys(this.tops).length
          for (let key in this.tops){
            if (this.tops[key].key.toLowerCase() === this.useName.toLowerCase()){
              this.placing = Number(100 - Math.ceil(((Number(key) + 1) / totals) * 100))
              this.get15 = this.placing
              if (this.placing >= 90){
                this.url = this.first
              } else if (this.placing >= 70) {
                this.url = this.second
              } else if (this.placing >= 50) {
                this.url = this.third
              } else {
                this.url = this.fouth
              }
            } else {
              this.get15 = 0
            }
          }
        })
      })
      Promise.all([getUser, getUserPr, getUserComments, getUserIssue, getUserDemoNum, getUserMeet,
        getUserStar, getUserFork, getUserTop]).then((res) => {
        if (!myreg.test(this.useName)) {
        } else {
          if (this.get3.length === 0 && this.get4 === 0 && this.get5.length === 0 &&
        this.get6 === 0 && this.get7.length === 0 && this.get8 === 0 && this.get10.length === 0 &&
        this.get11.length === 0 && this.get12 === 0 && this.get15 === 0) {
            this.$router.push({path: './Tourist'})
          } else {
            this.$router.push({ path: '/Detil', query: { name:this.useName } })
          }
        }
      })
    },
    tourist() {
      if (this.useName === ''){
        this.$router.push({path: './Tourist'})
      }
    }
  }
}
</script>
<style scoped>
* {
  margin: 0;
  padding: 0;
}
.login{
  width: 100%;
  height: 100%;
  position: relative;
}
.persent{
  width: 100%;
  height: 100%;
  display: block;
  }
  .btn{
    width: 100%;
    position: absolute;
    top:72%;

  }
  .btn img{
    display: block;
    width:58.7% ;
    margin: 0 auto;
    /* animation-name: likes;
    animation-direction: alternate;
    animation-timing-function: linear;
    animation-delay: 0s;
    animation-iteration-count: infinite;
    animation-duration: 1s; */
  }
  /* @keyframes likes{
    0%{
      transform: scale(1);
    }
     25%{
      transform: scale(1.1);
    }
     50%{
      transform: scale(0.9);
    }
     75%{
      transform: scale(1.1);
    }
     100%{
      transform: scale(1);
    }
  } */
  .user_text{
    width: 100%;
    height: 30px;
    position: absolute;
    top:62%;
  }
  .input {
  display: block;
  margin:0 auto;
  text-align: center;
  z-index: 1000;
  width: 58.7%;
  border-radius: 50px;
  background-color: #7463e9;
  color: rgb(218, 209, 209);
  border: 1px solid #9688f4;

}
input:-webkit-autofill, textarea:-webkit-autofill, select:-webkit-autofill{
    background-color: #4b3abe;
 }

input::-webkit-input-placeholder{
   color: rgb(218, 209, 209);
}
input::-moz-placeholder{   /* Mozilla Firefox 19+ */
   color: rgb(218, 209, 209);
}
input:-moz-placeholder{    /* Mozilla Firefox 4 to 18 */
   color: rgb(218, 209, 209);
}
input:-ms-input-placeholder{  /* Internet Explorer 10-11 */
   color: rgb(218, 209, 209);
}

.input:focus {
  outline: none;
}
 @media (max-width: 428px) and (min-width: 320px) {
.input{
  height: 40px;
  font-size: 14px;
}
 .btn img{
   height: 40px;
  }
.btn{
  height: 40px;
}
}
 @media (max-width: 768px) and (min-width: 429px) {
.input{
  height: 50px;
  font-size:22px ;
}
.btn{
  height: 60px;
}
 .btn img{
   height: 50px;
  }
 }
  @media (max-width:1024px) and (min-width: 769px) {
.input{
  height: 70px;
  font-size:28px ;
}
 .btn img{
   height: 80px;
  }
.btn{
  height: 80px;
}
 }

</style>
