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
  <div class="headerComp">
    <el-row
      :gutter="10"
    >
      <el-col
        :lg="6"
        :md="4"
        :sm="14"
        :xs="13"
      >
        <div class="logo">
          <img
            src="../../assets/images/logo.png"
            class="curp"
            alt
          >
        </div>
      </el-col>
      <el-col
        :lg="12"
        :md="12"
        class="navList"
      >
        <el-menu
          @select="handleSelect"
          mode="horizontal"
          :unique-opened="true"
          router
          text-color="#fff"
          background-color="#282b33"
          active-text-color="#6c92fa"
          :default-active="activeIndex"
        >
          <template
            v-for="(item,index) in list"
          >
            <el-submenu
              v-if="item.children && item.children.length"
              :disabled="!item.display"
              :index="item.route"
              :key="item.pageId"
              @click.native="jumpTo(item.route, index, item.link, item.display)"
            >
              <template
                slot="title"
              >
                {{ language === 'cn' ? item.labelCn : item.labelEn }}
              </template>
              <el-menu-item
                v-for="itemChild in item.children"
                :index="itemChild.route"
                :key="itemChild.pageId"
                @click="jumpTo(itemChild.route, index, itemChild.link, itemChild.display)"
              >
                {{ language === 'cn' ? itemChild.labelCn : itemChild.labelEn }}
              </el-menu-item>
            </el-submenu>
            <el-menu-item
              v-else
              :disabled="!item.display"
              :index="item.route"
              :key="item.pageId"
              @click="jumpTo(item.route, index, item.link, item.display)"
            >
              {{ language === 'cn' ? item.labelCn : item.labelEn }}
            </el-menu-item>
          </template>
        </el-menu>
      </el-col>
      <el-col
        :lg="6"
        :md="8"
        :sm="10"
        :xs="11"
      >
        <div class="nav-tabs">
          <span
            @click="changeLanguage"
            class="curp"
          >
            {{ getLanguage }}
          </span>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'HeaderComp',
  components: {
  },
  data () {
    return {
      language: 'cn',
      list: [
        {
          labelEn: 'APPD Transform',
          labelCn: 'APPD转换',
          route: '/home',
          pageId: '2.1.1',
          display: true,
          link: '',
          index: '4'
        }
      ],
      isActive: 0,
      activeIndex: ''
    }
  },
  computed: {
    getLanguage () {
      let language
      this.language === 'cn' ? language = 'English' : language = '中文'
      return language
    }
  },
  methods: {
    handleSelect (index, path, item) {
      if (index) {
        this.activeIndex = index
        this.$router.push(this.activeIndex)
      }
      this.closeMenu()
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
  watch: {
  },
  mounted () {
    localStorage.setItem('language', 'cn')
  },
  beforeDestroy () {
  }
}
</script>

<style lang='less' >
.headerComp {
  height: 65px;
  color: white;
  background: #282b33;
  position: fixed;
  z-index: 9999;
  width: 100%;
  padding: 0 10%;

  .logo {
    height: 65px;
    line-height: 65px;
    margin-left: -15px;
    img {
      height: 65px;
    }
    span {
      font-size: 18px;
      vertical-align: text-bottom;
    }
  }
    .navList {
      .el-menu--horizontal {
        border: none;
      }
      .el-menu--horizontal>.el-menu-item {
        height: 65px;
        line-height: 65px;
        font-size: 18px;
        font-weight: 700;
        margin-right: 0px;
        font-family: Microsoft YaHei , sans-serif;
        vertical-align: bottom;
      }
      .el-submenu__title {
        font-size: 18px;
        font-weight: 700;
        font-family: Microsoft YaHei , sans-serif;
      }
      .el-menu--horizontal>.el-submenu .el-submenu__title {
        height: 65px;
        line-height: 65px;
    }
    }
  .nav-tabs {
    padding-right: 20px;
    //height: 65px;
    line-height: 65px;
    display: flex;
    justify-content: flex-end;
    box-sizing: border-box;
    border-bottom: 0px solid #dee2e6;
    span{
      display: inline-block;
      padding: 0 6px;
      font-size: 14px;
    }
    .menu{
      display: none;
    }
    .el-icon-bell{
      font-size: 22px;
      margin-top: 22px;
      right: 15px;
      position: relative;
    }
    .el-icon-chat-round{
      color: red;
      position: absolute;
      font-size: 16px;
      margin-top: -7px;
      margin-left: -13px;
      text-align: center;
    }
    .countStyle{
      color: #FFFFF2;
      font-size: 10px;
      transform: translateY(-140%)
    }
    .countStyleBig{
      color: #FFFFF2;
      font-size: 10px;
      transform: translateY(-170%)
    }
  }
  .popUp{
    width: 350px;
    height: 300px;
    background-color: #404348;
    z-index: 999999;
    right: 10px;
    float: right;
    position:absolute;
  }
  .main-sidebar-small{
    position: relative;
    overflow-y: auto;
    z-index: 9999;
    .el-menu{
      background: rgba(0, 0, 0, 0.6);
      border-right: none;
    .el-submenu.is-active, .el-menu-item.is-active{
      background: rgba(0, 0, 0, 0.3);
      .first-menu{
        color: #6c92fa;
      }
    }
    .el-submenu__title{
      background-color: rgba(0, 0, 0, 0.4) !important;
    }
    .el-icon-arrow-down:before{
      color: #fff;
      font-size: 16px;
    }
  }
}
  @media only screen and (max-width: 991px){
    .nav-tabs{
      font-size: 16px;
      .menu{
      display: inline-block;
        .el-icon-menu{
          color: #fff;
          font-size: 25px;
          margin-top: 20px;
        }
      }
    }
    .navList{
      display: none;
    }
    .logo{
      img{
      height: 50px;
      margin: 5px 0 0 0;
    }
      span{
        font-size: 14px;
        margin: 5px 0 0 0;
      }
    }
  }
  @media screen and (max-width: 767px){
    .logo{
      img{
      height: 35px;
      margin: 15px 0 0 0;
      padding-bottom: 15px;
      }
      span{
        font-size: 13px;
      }
    }
    .nav-tabs{
      font-size: 12px;
    }
  }
  @media screen and (max-width: 385px) {
    .logo{
      img{
      height: 30px;
      }
      span{
        font-size: 13px;
      }
    }
    .nav-tabs{
      padding-right: 1px;
    }
  }
}
@media screen and (max-width:1380px){
  .headerComp{
    padding: 0 56px;
  }
}
</style>
