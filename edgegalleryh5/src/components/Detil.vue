<template>
<div class="detil"  ref='imageWrapper' id='obj'>
  <img src="../assets/detil.png" alt="" class="banner">
  <p class="user1">亲爱的</p>
  <p class="user2 dear_name">{{useName}}</p>
  <div class="thanks">
    <p class="words">因为有了您的付出, </p>
    <p class="words">EdgeGallery才有了今天的茁壮成长!</p>
  </div>
  <img src="../assets/meet.png" alt="" class="meet">
  <div class="meet_words">
    <div>
      <p class="words" v-show="oneMeet == 1"><span  class="words yellow">EdgeGallery</span>与您相识，距今已经<span class="words3 yellow">{{addDays}}天</span>了</p>
      <p class="words">您的每一次付出，我都有默默铭记！</p>
    </div>
  </div>
  <div class="first">
    <p class="words" v-show="oneStar == 1 ">{{firstStar}},您点亮了第一颗<span class="words yellow">STAR</span></p>
    <p class="words"  v-show="onePr == 1">{{firstPr}},您提交了第一个<span class="words yellow">PR</span></p>
    <p class="words"  v-show="oneIssue == 1">{{firstIssue}},您创建了第一个<span class="words yellow">ISSUE</span></p>
    <p class="words" v-show="oneComments == 1">{{firstComments}},您提交了第一次<span class="words yellow">COMMENT</span></p>
  </div>
  <img src="../assets/computer.png" alt="" class="computer">
  <img src="../assets/know.png" alt="" class="know">
  <div class="know_words">
    <p class="words">回望这一年，我的画廊中又新增了不少颜色</p>
    <p class="words yellow">Amber，Blue，Chocolate，Dove，EverGreen</p>
    <p class="words">这些颜色中饱含着您的付出</p>
  </div>
  <div class="all">
    <p class="words">在这些时间里</p>
    <div v-show="litStar == 1">
      <p class="words">您亲手点亮了<span class="words3 yellow">{{addStar}}</span>个<span class="words ">STAR</span></p>
      <p class="words">这些仓库的成长从此有了您的关心</p>
    </div>
    <div  v-show="givePr == 1">
      <p class="words">您一共提交了<span class="words3 yellow">{{addPr}}</span>个<span class="words ">PR</span></p>
      <p class="words">每个PR就像是砌墙的石砖一样，支撑起万丈高楼</p>
    </div>
    <div v-show="giveIssue == 1">
      <p class="words">您一共创建了<span class="words3 yellow">{{addIssue}}</span>个<span class="words ">ISSUE</span></p>
      <p class="words">每个Issue都在不停的修正<span class="words ">EdgeGallery</span>前进的力量</p>
    </div>
    <p class="words" v-show="giveComments == 1">您一共提交了<span class="words3 yellow">{{addComments}}</span>个<span class="words ">COMMENT</span></p>
    <p class="words">您的每一个建议和指导都是<span class="words">EdgeGallery</span>前进的力量</p>
  </div>
  <img src="../assets/user.png" alt="" class="user">
  <img src="../assets/look.png" alt="" class="look">
  <div class="look_words">
    <p class="words">我的世界因您的参与变得越来越多彩!</p>
    <p class="words">在这一年里</p>
  </div>
  <div class="medal_words">
    <p  class="words">您的贡献已经<span class="words yellow">超越</span>了<span class="words2 yellow">{{placing}}</span><span class="words yellow">%</span>的用户</p>
    <p  class="words">成为<span class="words yellow">EdgeGallery</span>的忠实拥护者，请收下为您准备的奖章</p>
  </div>
  <img src="../assets/medal_background.png" alt="" class="medal_backgrond">
  <img :src='url' alt="" class="medal">
  <div class="say">
    <p class="words">在这一周年之际,EdgeGallery想对您说一声感谢!</p>
    <p class="words">在开源的道路上,感谢您躬身入局,共绘蓝图</p>
  </div>
  <img src="../assets/ewm_left.png" alt="" class="ewm_left">
  <img src="../assets/ewm.png" alt="" class="ewm">
  <img src="../assets/ewm_right.png" alt="" class="ewm_right">
  <div class="look_ewm">
    <p class="words">请扫描二维码</p>
    <p class="words">生成您和EdgeGallery的专属记忆!</p>
  </div>
  <img src="../assets/ip.png" alt="" class="ip">
  <p class="tootip">点击<span class="yellow " @click="convertHtmlToCanvas">生成图片</span>后,长按即可保存到相册!</p>
</div>
</template>
<script>
import { getDataApi, getPr, getComments, getIssue, getDemoNum, getMeet, getStar, getFork, getTop } from '../api/wxapi'
import html2canvas from 'html2canvas'
import king from '../assets/No1.png'
import diamond from '../assets/No2.png'
import gold from '../assets/No3.png'
import silver from '../assets/No4.png'
import NativeShare from 'nativeshare'

export default {
  name: 'detil',
  data() {
    return {
      imgNone:true,
      top:document.documentElement.scrollTop,
      oneStar:1,
      oneComments:1,
      oneIssue:1,
      oneMeet:1,
      onePr:1,
      litStar:1,
      giveIssue:1,
      giveComments:1,
      givePr:1,
      nativeShare: new NativeShare(),
      useName: '',
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
      first: king,
      second: diamond,
      third: gold,
      fouth: silver,
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
    this.useName = this.$route.query.name
    this.getUser()
    this.getUserPr()
    this.getUserComments()
    this.getUserIssue()
    this.getUserDemoNum()
    this.getUserMeet()
    this.getUserStar()
    this.getUserFork()
    this.getUserTop()
  },
  watch:{
  },
  destroyed() {
  },
  mounted(){
  },
  methods: {
    getUser() {
      getDataApi(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstLightHouse = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          this.firstLightHouse = this.firstLightHouse.substr(0, 4) + '年' + this.firstLightHouse.substr(5, 2) + '月' + this.firstLightHouse.substr(8, 2) + '日'
        }
        this.addLightHouse = res.data.hits.total.value
        this.get2 = res.data.hits.total.value
        this.get1 = res.data.hits.hits
      })
    },
    getUserPr(){
      getPr(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstPr = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          this.firstPr = this.firstPr.substr(0, 4) + '年' + this.firstPr.substr(5, 2) + '月' + this.firstPr.substr(8, 2) + '日'
        } else {
          this.onePr = 0
        }
        this.get3 = res.data.hits.hits
        this.addPr = res.data.hits.total.value
        if (this.addPr === 0){
          this.givePr = 0
        }
        this.get4 = res.data.hits.total.value
      })
    },
    getUserComments(){
      getComments(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstComments = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          this.firstComments = this.firstComments.substr(0, 4) + '年' + this.firstComments.substr(5, 2) + '月' + this.firstComments.substr(8, 2) + '日'
        } else {
          this.oneComments = 0
        }
        this.addComments = res.data.hits.total.value
        if (this.addComments === 0){
          this.giveComments = 0
        }
        this.get5 = res.data.hits.hits
        this.get6 = res.data.hits.total.value
      })
    },
    getUserIssue(){
      getIssue(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstIssue = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          this.firstIssue = this.firstIssue.substr(0, 4) + '年' + this.firstIssue.substr(5, 2) + '月' + this.firstIssue.substr(8, 2) + '日'
        } else {
          this.oneIssue = 0
        }
        this.addIssue = res.data.hits.total.value
        if (this.addIssue === 0){
          this.giveIssue = 0
        }
        this.get7 = res.data.hits.hits
        this.get8 = res.data.hits.total.value
      })
    },
    getUserDemoNum(){
      getDemoNum(this.useName).then(res => {
        this.demoNum = res.data.aggregations.sum.value
        this.get9 = res.data.aggregations.sum.value
      })
    },
    getUserMeet(){
      getMeet(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstMeet = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          this.firstMeet = this.firstMeet.substr(0, 4) + '年' + this.firstMeet.substr(5, 2) + '月' + this.firstMeet.substr(8, 2) + '日'
          this.date = res.data.hits.hits[0]._source.created_at.substr(0, 10)
          let date2 = new Date()
          let date1 = new Date(Date.parse(this.date.replace(/-/g, '/')))
          this.addDays = parseInt(Math.abs(date2.getTime() - date1.getTime()) / 1000 / 60 / 60 / 24)
        } else {
          this.oneMeet = 0
        }
        this.get10 = res.data.hits.hits
      })
    },
    getUserStar(){
      getStar(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstStar = res.data.hits.hits[0]._source.created_at.substr(0, 10)
        } else {
          this.oneStar = 0
        }
        this.firstStar = this.firstStar.substr(0, 4) + '年' + this.firstStar.substr(5, 2) + '月' + this.firstStar.substr(8, 2) + '日'
        this.addStar = res.data.hits.total.value
        if (this.addStar === 0){
          this.litStar = 0
        }
        this.get11 = res.data.hits.hits
        this.get12 = res.data.hits.total.value
      })
    },
    getUserFork(){
      getFork(this.useName).then(res => {
        if (res.data.hits.hits.length !== 0){
          this.firstFork = res.data.hits.hits[0]._source.created_at.substr(0, 10)
        }
        this.firstFork = this.firstFork.substr(0, 4) + '年' + this.firstFork.substr(5, 2) + '月' + this.firstFork.substr(8, 2) + '日'
        this.addFork = res.data.hits.total.value
        this.get13 = res.data.hits.hits
        this.get14 = res.data.hits.total.value
      })
    },
    getUserTop(){
      getTop(this.useName).then(res => {
        this.tops = res.data.aggregations.uniq_gender.buckets
        let totals = Object.keys(this.tops).length
        for (let key in this.tops){
          if (this.tops[key].key.toLowerCase() === this.useName.toLowerCase()){
            this.placing = Number(100 - Math.floor(((Number(key) + 1) / totals) * 100))
            if (this.placing >= 90){
              this.url = this.first
            } else if (this.placing >= 70) {
              this.url = this.second
            } else if (this.placing >= 50) {
              this.url = this.third
            } else {
              this.url = this.fouth
            }
            if (this.placing <= 10){
              this.placing = 10
            }
            let shows = Number(this.oneStar) + Number(this.oneComments) + Number(this.oneIssue) + Number(this.onePr) +
            Number(this.litStar) + Number(this.giveIssue) + Number(this.giveComments) + Number(this.givePr)
            if (shows <= 2){
              this.placing = 30
              this.url = this.fouth
            } else if (shows <= 4){
              this.placing = 60
              this.url = this.third
            }
          } else {
            this.get15 = 0
          }
        }
      })
    },
    convertHtmlToCanvas(){
      let that = this
      window.pageYoffset = 0
      document.documentElement.scrollTop = 0
      document.body.scrollTop = 0
      var scrollTop = document.documentElement.scrollTop && document.body.scrollTop
      html2canvas(document.querySelector('#obj'), {
        allowTaint: false,
        useCORS: true,
        windowWidth: document.body.scrollWidth,
        windowHeight: document.body.scrollHeight,
        x: 0,
        y: scrollTop,
        dpi: window.devicePixelRatio * 1,
        scale: 1
      }).then(function (canvas){
        document.getElementById('app').style.display = 'none'
        that.convertCanvasToImage(canvas)
      })
    },
    convertCanvasToImage(canvas){
      var image = new Image()
      image.className = 'edgeGallery'
      image.src = canvas.toDataURL('image/png', 0.92)
      document.body.appendChild(image)
    }
  }
}
</script>
<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

 @media (max-width: 428px) and (min-width: 320px) {
        * {
          margin: 0;
          padding: 0;
        }
        .detil{
          width: 100%;
          position: relative;
        }
        .banner{
          width: 100%;
        }
        .user1{
          background-image: url(../assets/user1.png);
          background-size: 100% 100%;
          width: 33%;
          height: 34px;
          position: absolute;
          left:10.5% ;
          top:8.1% ;
          z-index: 1;
          color: #fff;
          padding-left:3% ;
          line-height: 34px;
        }
        .user2{
          background-image: url(../assets/user2.png);
          background-size: 100% 100%;
          width: 33%;
          height: 44px;
          position: absolute;
          left:23.9% ;
          top:8.6% ;
          z-index: 100;
        }

        .thanks{
          position: absolute;
          left:10.5% ;
          top: 11.1%;
        }
        .meet{
          position: absolute;
          top: 14.5%;
          left: 8.5%;
          width: 24%;
          height: 36px;
        }
        .meet_words{
          position: absolute;
          top: 17%;
          left: 10.5%;
        }
        .first{
          position: absolute;
          top: 20%;
          left: 10.5%;
        }
        .computer{
            position: absolute;
            top: 23.8%;
            right: 0;
            height: 120px;
            width: 26.1%;
        }
        .know{
          position: absolute;
          top: 27.1%;
          left: 8.5%;
          width: 24%;
          height: 36px;
        }
        .know_words{
          position: absolute;
          top: 29.3%;
          left: 10.5%;
        }
        .all{
          position: absolute;
          top: 33.6%;
          left: 10.5%;
        }
        .user{
          position: absolute;
          top: 47%;
          left: 0;
          width: 100%;
          height: 260px;
        }
        .look{
          position: absolute;
          top: 59.4%;
          left: 8.5%;
          width: 24%;
          height: 36px;
        }
        .look_words{
          position: absolute;
          top: 61.8%;
          left: 7%;
        }
        .medal_words{
          position: absolute;
          top: 64.4%;
          left: 7%;
        }
        .medal_backgrond{
          position: absolute;
          top: 67.4%;
          left: 12.5%;
          width:75% ;
          height: 200px;
          }
        .medal{
          position: absolute;
          top: 69%;
          left: 25%;
          width:50% ;
          height: 210px;
        }
        .say{
          position: absolute;
          top: 78%;
          left: 17%;
        }
        .words{
          font-size: 14px;
          color: #FFFFFF;
          line-height: 28px;
          font-family: HarmonyHeiTi;
        }
        .words3{
          font-size: 18px;
          color: #FFFFFF;
          line-height: 28px;
          font-family: HarmonyHeiTi;
        }
        .ewm_left{
          position: absolute;
          top: 82.3%;
          left: 24%;
          width: 7.5%;
          height: 60px;
        }
        .ewm{
          position: absolute;
          top: 81.3%;
          left: 38.3%;
          width: 23.9%;
          height: 90px;
          padding: 4px;
          border-radius: 10px;
          border: 1px solid #fff;
        }
        .ewm_right{
          position: absolute;
          top: 82.3%;
          left: 69.1%;
          width: 7.5%;
          height: 60px;
        }
        .look_ewm{
          position: absolute;
          top: 86.6%;
          left: 22%;
          width: 60%;
        }
        .look_ewm p{
          text-align: center;
        }
        .ip{
          position: absolute;
          top: 89.8%;
          left: 29.3%;
          width: 46%;
          height: 160px;
        }
        .save{
          position: absolute;
          top: 95.5%;
          left: 29.3%;
          width:45% ;
          height: 40px;
          text-align: center;
          line-height: 30px;
          font-size: 16px;
          font-family: HarmonyHeiTi;
          color: #FFFFFF;
          border: 2px solid #FFFFFF;
          border-radius:10px ;
          background-color: #0c0551;

        }
       .tootip{
         font-size:12px ;
         position: absolute;
         bottom: 2%;
         left: 27.3%;
         color: #fff;
        }
        .words2{
          font-size: 40px;
          color: #FFFFFF;
          font-family: HarmonyHeiTi;
        }
        .yellow{
          color:#FFD36A;
        }
        .dear_name{
          color: #110870;
          text-align: center;
          line-height: 30px;
        }

}
 @media (max-width: 768px) and (min-width: 429px) {
        * {
          margin: 0;
          padding: 0;
        }
        .detil{
          width: 100%;
          position: relative;
        }
        .banner{
          width: 100%;
        }
        .user1{
          background-image: url(../assets/user1.png);
          background-size: 100% 100%;
          width: 40%;
          height: 56px;
          position: absolute;
          left:10.5% ;
          top:8.1% ;
          z-index: 1;
          font-size: 18px;
          color: #fff;
          padding-left:4% ;
          line-height: 56px;
        }
        .user2{
          background-image: url(../assets/user2.png);
          background-size: 100% 100%;
          width: 40%;
          height: 66px;
          position: absolute;
          left:23.9% ;
          font-size: 18px;
          top:8.6% ;
          z-index: 100;
        }

        .thanks{
          position: absolute;
          left:10.5% ;
          top: 11.1%;
        }
        .meet{
          position: absolute;
          top: 14.5%;
          left: 8.5%;
          width: 28%;
          height: 60px;
        }
        .meet_words{
          position: absolute;
          top: 17%;
          left: 10.5%;
        }
        .first{
          position: absolute;
          top: 20%;
          left: 10.5%;
        }
        .computer{
          position: absolute;
          top: 22.3%;
          right: 0;
          height: 240px;
          width: 45%;
        }
        .know{
          position: absolute;
          top: 27.1%;
          left: 8.5%;
          width: 28%;
          height: 60px;
        }
        .know_words{
          position: absolute;
          top: 29.3%;
          left: 10.5%;
        }
        .tootip{
         font-size:24px ;
         position: absolute;
         bottom: 2%;
         left: 27.3%;
         color: #fff;
        }
        .all{
          position: absolute;
          top: 33.6%;
          left: 10.5%;
        }
        .user{
          position: absolute;
          top: 43%;
          left: 0;
          width: 100%;
          height: 650px;
        }
        .look{
          position: absolute;
          top: 59.4%;
          left: 8.5%;
          width: 28%;
          height: 60px;
        }
        .look_words{
          position: absolute;
          top: 61.8%;
          left: 20%;
        }
        .medal_words{
          position: absolute;
          top: 64.4%;
          left: 20%;
        }
        .medal_backgrond{
          position: absolute;
          top: 67.4%;
          left: 12.5%;
          width:75% ;
          height: 350px;
          }
        .medal{
          position: absolute;
          top: 69%;
          left: 25%;
          width:50% ;
          height: 400px;
        }
        .say{
          position: absolute;
          top: 78%;
          left: 25%;
        }
        .words{
          font-size: 22px;
          color: #FFFFFF;
          line-height: 40px;
          font-family: HarmonyHeiTi;
        }
        .words3{
          font-size: 32px;
          color: #FFFFFF;
          line-height: 40px;
          font-family: HarmonyHeiTi;
        }
        .ewm_left{
          position: absolute;
          top: 82.3%;
          left: 24%;
          width: 7.5%;
          height: 120px;
        }
        .ewm{
          position: absolute;
          top: 81.3%;
          left: 38.3%;
          width: 23.9%;
          height: 190px;
          padding: 4px;
          border-radius: 10px;
          border: 1px solid #fff;
        }
        .ewm_right{
          position: absolute;
          top: 82.3%;
          left: 69.1%;
          width: 7.5%;
          height: 120px;
        }
        .look_ewm{
          position: absolute;
          top: 86.6%;
          left: 22%;
          width: 60%;
        }
        .look_ewm p{
          text-align: center;
        }
        .ip{
          position: absolute;
          top: 89.8%;
          left: 29.3%;
          width: 46%;
          height: 340px;
        }
        .save{
          position: absolute;
          top: 96.5%;
          left: 29.3%;
          width:45% ;
          height: 65px;
          text-align: center;
          line-height: 60px;
          font-size: 24px;
          font-family: HarmonyHeiTi;
          color: #FFFFFF;
          border: 2px solid #FFFFFF;
          border-radius:10px ;
          background-color: #0c0551;

        }
        .words2{
          font-size: 75px;
          color: #FFFFFF;
          font-family: HarmonyHeiTi;
        }
        .yellow{
          color:#FFD36A;
        }
        .dear_name{
          color: #110870;
          text-align: center;
          line-height: 46px;
        }
        }
 @media (max-width:1024px) and (min-width: 769px) {
 * {
          margin: 0;
          padding: 0;
        }
        .detil{
          width: 100%;
          position: relative;
        }
        .banner{
          width: 100%;
        }
        .user1{
          background-image: url(../assets/user1.png);
          background-size: 100% 100%;
          width: 40%;
          height: 80px;
          position: absolute;
          left:10.5% ;
          top:8.1% ;
          z-index: 1;
          font-size: 28px;
          color: #fff;
          padding-left:4% ;
          line-height: 80px;
        }
        .tootip{
         font-size:32px ;
         position: absolute;
         bottom: 2%;
         left: 27.3%;
         color: #fff;
        }
        .user2{
          background-image: url(../assets/user2.png);
          background-size: 100% 100%;
          width: 40%;
          height: 90px;
          position: absolute;
          left:23.9% ;
          font-size: 28px;
          top:8.6% ;
          z-index: 100;
        }

        .thanks{
          position: absolute;
          left:10.5% ;
          top: 11.1%;
        }
        .meet{
          position: absolute;
          top: 14.5%;
          left: 8.5%;
          width: 30%;
          height: 90px;
        }
        .meet_words{
          position: absolute;
          top: 17%;
          left: 10.5%;
        }
        .first{
          position: absolute;
          top: 20%;
          left: 10.5%;
        }
        .computer{
          position: absolute;
          top: 22.3%;
          right: 0;
          height: 300px;
          width: 45%;
        }
        .know{
          position: absolute;
          top: 27.1%;
          left: 8.5%;
          width: 30%;
          height: 90px;
        }
        .know_words{
          position: absolute;
          top: 29.3%;
          left: 10.5%;
        }
        .all{
          position: absolute;
          top: 33.6%;
          left: 10.5%;
        }
        .user{
          position: absolute;
          top: 44%;
          left: 0;
          width: 100%;
          height: 800px;
        }
        .look{
          position: absolute;
          top: 59.4%;
          left: 8.5%;
          width: 30%;
          height: 90px;
        }
        .look_words{
          position: absolute;
          top: 61.8%;
          left: 16%;
        }
        .medal_words{
          position: absolute;
          top: 64.4%;
          left: 16%;
        }
        .medal_backgrond{
          position: absolute;
          top: 67.4%;
          left: 12.5%;
          width:75% ;
          height: 450px;
          }
        .medal{
          position: absolute;
          top: 69%;
          left: 25%;
          width:50% ;
          height: 500px;
        }
        .say{
          position: absolute;
          top: 78%;
          left: 25%;
        }
        .words{
          font-size: 32px;
          color: #FFFFFF;
          line-height: 60px;
          font-family: HarmonyHeiTi;
        }
        .words3{
          font-size: 40px;
          color: #FFFFFF;
          line-height: 60px;
          font-family: HarmonyHeiTi;
        }
        .ewm_left{
          position: absolute;
          top: 82.3%;
          left: 24%;
          width: 7.5%;
          height: 160px;
        }
        .ewm{
          position: absolute;
          top: 81.3%;
          left: 38.3%;
          width: 23.9%;
          height: 240px;
          padding: 4px;
          border-radius: 10px;
          border: 1px solid #fff;
        }
        .ewm_right{
          position: absolute;
          top: 82.3%;
          left: 69.1%;
          width: 7.5%;
          height: 160px;
        }
        .look_ewm{
          position: absolute;
          top: 86.6%;
          left: 22%;
          width: 60%;
        }
        .look_ewm p{
          text-align: center;
        }
        .ip{
          position: absolute;
          top: 89.8%;
          left: 29.3%;
          width: 46%;
          height: 440px;
        }
        .save{
          position: absolute;
          top: 96.8%;
          left: 29.3%;
          width:45% ;
          height: 85px;
          text-align: center;
          line-height: 85px;
          font-size: 32px;
          font-family: HarmonyHeiTi;
          color: #FFFFFF;
          border: 2px solid #FFFFFF;
          border-radius:10px ;
          background-color: #0c0551;

        }
        .words2{
          font-size: 75px;
          color: #FFFFFF;
          font-family: HarmonyHeiTi;
        }
        .yellow{
          color:#FFD36A;
        }
        .dear_name{
          color: #110870;
          text-align: center;
          line-height: 66px;
        }

 }
</style>
