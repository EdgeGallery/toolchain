/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
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

const HttpProxyAgent = require('http-proxy-agent')
const path = require('path')

function resolve (dir) {
  return path.join(__dirname, dir)
}
module.exports = {
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', resolve('src')).end()

    config.module
      .rule('yaml')
      .test(/\.yaml$/)
      .include.add(resolve('src/assets/file'))
      .end()
      .use('url-loader')
      .loader('url-loader')
      .options({
        limit: 10000,
        name: `static/yaml/[name].[hash:7].[ext]`
      })
      .end()
  },
  devServer: {
    host: '0.0.0.0',
    proxy: {
      '/rest/mec-appstore/': {
        target: 'http://mec-appstore',
        agent: new HttpProxyAgent('http://127.0.0.1:8082'),
        changeOrigin: true,
        pathRewrite: {
          '^/rest/mec-appstore': '/'
        }
      },
      '/rest/mec-developer/': {
        target: 'http://mec-developer',
        agent: new HttpProxyAgent('http://127.0.0.1:8082'),
        changeOrigin: true,
        pathRewrite: {
          '^/rest/mec-developer': '/'
        }
      },
      '/rest/user-mgmt-be': {
        target: 'http://user-mgmt-be',
        agent: new HttpProxyAgent('http://127.0.0.1:8082'),
        changeOrigin: true,
        pathRewrite: {
          '^/rest/user-mgmt-be': '/'
        }
      }
    }
  },
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',

  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: false
    }
  },
  productionSourceMap: true
}
