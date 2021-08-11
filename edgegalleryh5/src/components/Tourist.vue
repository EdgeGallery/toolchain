<template>
  <div class="travlist">
    <img src="../assets/travlist_background.png" alt="" class="travlist_background">
    <img src="../assets/IP1.png" alt="" class="ip1 ip">
    <p class="ip_name ip1_name">PURPLE BEE</p>
    <p class="ip_content ip1_content">我是负责数据传输的紫色BEE,<a class="yellow" href="https://gitee.com/edgegallery">点击进入社区的邮件列表</a>,参与社区重要会议和讨论吧</p>
    <img src="../assets/IP2.png" alt="" class="ip2 ip">
    <p class="ip_name ip2_name">RADAR BEE</p>
    <p class="ip_content ip2_content">我是负责测试漏洞的雷达BEE,如果您是测试和质量专家,<a  class="yellow" href="https://gitee.com/edgegallery/community/tree/master/Test%20WG">点击进入社区的测试工作组</a>转一转</p>
    <img src="../assets/IP3.png" alt="" class="ip3 ip">
    <p class="ip_name ip3_name">WORKER BEE</p>
    <p class="ip_content ip3_content">我是负责客服工作的工蜂BEE，我跟EdgeGallery小助手最熟悉了，您可以添加微信：<span class="other">EdgeGallery </span>认识一下</p>
    <img src="../assets/IP4.png" alt="" class="ip4 ip">
    <p class="ip_name ip4_name">GREEEN BEE</p>
    <p class="ip_content ip4_content">负责社区建设的绿绿BEE,社区的官网和活动都是我负责的,想要及时获得社区的活动信息,就赶快关注下方<span class="other"> 微信公众号</span> 吧,它是我的代言人。</p>
    <img src="../assets/wx_ewm.jpg" alt="" class="wx_ewm">
  </div>
</template>
<script>
export default {
  data () {
    return {
      day:''
    }
  },
  methods: {
    getNowFormatDate() {
      var date = new Date()
      var year = date.getFullYear()
      var month = date.getMonth() + 1
      var strDate = date.getDate()
      if (month >= 1 && month <= 9) {
        month = '0' + month
      }
      if (strDate >= 0 && strDate <= 9) {
        strDate = '0' + strDate
      }
      this.day = year + '年' + month + '月' + strDate + '日'
    }
  },
  mounted(){
    this.getNowFormatDate()
  }
}
</script>
<style scoped>
@media (max-width: 428px) and (min-width: 320px) {
        * {
          margin: 0;
          padding: 0;
        }
        a{
          text-decoration: none;
        }
        .travlist{
          width: 100%;
          position: relative;
        }
        .travlist_background{
          width: 100%;
          height: 100%;
          display: block;
        }
        .ip{
          width: 36%;
          height: 19%;
        }
        .other{
          color: #fff;
          font-weight:600 ;
        }
        .ip_content{
          width: 49.1%;
          font-size: 14px;
          line-height: 20px;
          color: #fff;
        }
        .yellow{
          color:#FFD942;
        }
        .ip_name{
          color: #00DFFF;
          line-height: 48px;
          font-size:22px ;
          font-weight: bold;
        }
        .ip1{
          position:absolute;
          left: 2%;
          top:26.8%;

        }
        .ip1_name{
          position:absolute;
          left: 41.2%;
          top:28.6%;
        }
        .ip1_content{
          position:absolute;
          left: 41.2%;
          top:32.6%;
        }
        .ip2{
          position:absolute;
          left: 64%;
          top:39%;

        }
        .ip2_name{
          position:absolute;
          left: 30%;
          top:43%;
        }
        .ip2_content{
          position:absolute;
          left: 12%;
          top:47.2%;
        }
        .ip3{
          position:absolute;
          left: 2%;
          top:54.5%;

        }
        .ip3_name{
          position:absolute;
          left: 41.2%;
          top:56%;
        }
        .ip3_content{
          position:absolute;
          left: 41.2%;
          top:60.4%;
        }
        .ip4{
          position:absolute;
          left: 64%;
          top:68%;

        }
        .ip4_name{
          position:absolute;
          left: 30%;
          top:70%;
        }
        .ip4_content{
          position:absolute;
          left: 12%;
          top:74.2%;
        }
        .wx_ewm{
          position: absolute;
          left: 38%;
          top: 85%;
          width: 21.1%;
          height: 79px;
          padding: 2px;
          border-radius: 10px;
        }

}
@media (max-width: 768px) and (min-width: 429px) {
        * {
          margin: 0;
          padding: 0;
        }
        a{
          text-decoration: none;
        }
        .other{
          color: #fff;
          font-weight:600 ;
        }
        .travlist{
          width: 100%;
          position: relative;
        }
        .travlist_background{
          width: 100%;
          height: 100%;
          display: block;
        }
        .ip{
          width: 36%;
          height: 19%;
        }
        .ip_content{
          width: 49.1%;
          font-size: 24px;
          line-height: 40px;
          color: #fff;
        }
        .yellow{
          color:#FFD942;
        }
        .ip_name{
          color: #00DFFF;
          line-height: 48px;
          font-size:48px ;
          font-weight: bold;
        }
        .ip1{
          position:absolute;
          left: 2%;
          top:26.8%;

        }
        .ip1_name{
          position:absolute;
          left: 41.2%;
          top:28.6%;
        }
        .ip1_content{
          position:absolute;
          left: 41.2%;
          top:32.6%;
        }
        .ip2{
          position:absolute;
          left: 64%;
          top:39%;

        }
        .ip2_name{
          position:absolute;
          left: 30%;
          top:43%;
        }
        .ip2_content{
          position:absolute;
          left: 12%;
          top:47.2%;
        }
        .ip3{
          position:absolute;
          left: 2%;
          top:54.5%;

        }
        .ip3_name{
          position:absolute;
          left: 41.2%;
          top:56%;
        }
        .ip3_content{
          position:absolute;
          left: 41.2%;
          top:60.4%;
        }
        .ip4{
          position:absolute;
          left: 64%;
          top:68%;

        }
        .ip4_name{
          position:absolute;
          left: 30%;
          top:70%;
        }
        .ip4_content{
          position:absolute;
          left: 12%;
          top:74.2%;
        }
        .wx_ewm{
          position: absolute;
          left: 36%;
          top: 84%;
          width: 26.1%;
          height: 173px;
          padding: 2px;
          border-radius: 10px;
        }

}
@media (max-width: 1024px) and (min-width: 769px) {
        * {
          margin: 0;
          padding: 0;
        }
        .other{
          color: #fff;
          font-weight:600 ;
        }
        a{
          text-decoration: none;
        }
        .travlist{
          width: 100%;
          position: relative;
        }
        .travlist_background{
          width: 100%;
          height: 100%;
          display: block;
        }
        .ip{
          width: 36%;
          height: 19%;
        }
        .ip_content{
          width: 49.1%;
          font-size: 32px;
          line-height: 48px;
          color: #fff;
        }
        .yellow{
          color:#FFD942;
        }
        .ip_name{
          color: #00DFFF;
          line-height: 60px;
          font-size:60px ;
          font-weight: bold;
        }
        .ip1{
          position:absolute;
          left: 2%;
          top:26.8%;

        }
        .ip1_name{
          position:absolute;
          left: 41.2%;
          top:28.6%;
        }
        .ip1_content{
          position:absolute;
          left: 41.2%;
          top:32.6%;
        }
        .ip2{
          position:absolute;
          left: 64%;
          top:39%;

        }
        .ip2_name{
          position:absolute;
          left: 30%;
          top:43%;
        }
        .ip2_content{
          position:absolute;
          left: 12%;
          top:47.2%;
        }
        .ip3{
          position:absolute;
          left: 2%;
          top:54.5%;

        }
        .ip3_name{
          position:absolute;
          left: 41.2%;
          top:56%;
        }
        .ip3_content{
          position:absolute;
          left: 41.2%;
          top:60.4%;
        }
        .ip4{
          position:absolute;
          left: 64%;
          top:68%;

        }
        .ip4_name{
          position:absolute;
          left: 30%;
          top:70%;
        }
        .ip4_content{
          position:absolute;
          left: 12%;
          top:74.2%;
        }
        .wx_ewm{
          position: absolute;
          left: 36%;
          top: 84%;
          width: 26.1%;
          height: 247px;
          padding: 2px;
          border-radius: 10px;
        }

}
</style>
