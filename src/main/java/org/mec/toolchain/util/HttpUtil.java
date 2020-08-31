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

package org.mec.toolchain.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.mec.toolchain.config.HttpPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    @Autowired
    private HttpPoolConfig httpPoolConfigWrapper;

    private static HttpPoolConfig httpPoolConfig;

    /**
     * init SSL http pool config.
     */
    @PostConstruct
    public void init() {
        if (httpPoolConfig == null) {
            httpPoolConfig = this.httpPoolConfigWrapper;
        }
    }

    private PoolingHttpClientConnectionManager getConnManager() {
        return HttpUtilHandler.connManager;
    }

    private static SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        };
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        return sslContext;

    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(httpPoolConfig.getConnectionRequestTimeout())
                .setConnectTimeout(httpPoolConfig.getConnectTimeout())
                .setSocketTimeout(httpPoolConfig.getSocketTimeout())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(getConnManager())
            .setDefaultRequestConfig(requestConfig)
            .build();

        return httpClient;

    }

    private String getResponse(HttpRequestBase httpRequest) {

        String res = null;
        CloseableHttpResponse response = null;

        try (CloseableHttpClient httpClient = getHttpClient()) {
            response = httpClient.execute(httpRequest);
            int respCode = response.getStatusLine().getStatusCode();

            if (HttpResponseStatus.OK.code() == respCode) {
                HttpEntity responseEntity = response.getEntity();
                res = EntityUtils.toString(responseEntity);
            }
        }  catch (IOException e) {
            LOGGER.error("IOException: " + e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error("Close response error: " + e);
                }
            }
        }
        return res;
    }

    /**
     * https post entry point.
     *
     * @param url url
     * @param headers request header
     * @param bodyParam request body
     * @return string
     */
    public String httpsPost(String url, Map<String, String> headers, String bodyParam) {
        HttpPost post = new HttpPost(url);
        if (bodyParam != null) {
            HttpEntity entity = new StringEntity(bodyParam, "UTF-8");
            post.setEntity(entity);
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return getResponse(post);
    }

    /**
     * https get entry point.
     *
     * @param url url
     * @param headers request header
     * @return string
     */
    public String httpsGet(String url, Map<String, String> headers) {
        HttpGet get = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return getResponse(get);
    }

    static class HttpUtilHandler {
        static PoolingHttpClientConnectionManager connManager;

        static {
            try {
                SSLContext sslContext = getSslContext();
                SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext,
                        NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", sslFactory)
                        .build();

                connManager = new PoolingHttpClientConnectionManager(registry);
                connManager.setMaxTotal(httpPoolConfig.getMaxTotal());
                connManager.setDefaultMaxPerRoute(httpPoolConfig.getDefaultMaxPerRoute());
            } catch (SSLInitializationException | KeyManagementException | NoSuchAlgorithmException e) {
                LOGGER.error("Create SSL connection failed: " + e);
            }
        }
    }
}
