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

package org.mec.toolchain.controller;

import static org.mockito.ArgumentMatchers.eq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.toolchain.config.PortingParamConfig;
import org.mec.toolchain.model.porting.BaseResponse;
import org.mec.toolchain.model.porting.LoginData;
import org.mec.toolchain.model.porting.LoginResponse;
import org.mec.toolchain.util.HttpUtil;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PortingControllerTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected HttpUtil httpUtil;

    @Autowired
    protected PortingParamConfig portingParamConfig;

    protected Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Test
    public void initTest() {
        // empty method
    }

    @Before
    public void init() {
        String loginUrl = portingParamConfig.getUserUrl() + "login/";
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setStatus(0);
        LoginData loginData = new LoginData();
        loginData.setToken("JWT abcdefg");
        loginResponse.setData(loginData);
        Mockito.when(httpUtil.httpsPost(eq(loginUrl), Mockito.anyMap(), Mockito.anyString()))
            .thenReturn(gson.toJson(loginResponse));

        String logoutUrl = portingParamConfig.getUserUrl() + "logout/";
        Mockito.when(httpUtil.httpsPost(eq(logoutUrl), Mockito.anyMap(), Mockito.nullable(String.class)))
            .thenReturn(gson.toJson(new BaseResponse(0, "logout success")));
    }

}
