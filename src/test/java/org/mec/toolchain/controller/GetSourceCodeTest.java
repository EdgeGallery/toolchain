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

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class GetSourceCodeTest extends PortingControllerTest {

    @Test
    public void testGetSourceCodeSuccess() throws Exception {

        String projectId = UUID.randomUUID().toString();
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/mec/toolchain/v1/porting/" + projectId)
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(result.andReturn().getResponse().getStatus(), 200);
        Assert.assertTrue(result.andReturn().getResponse().getContentAsString().contains("false"));
    }

    @Test
    public void testGetSourceCodeFail() throws Exception {

        String projectId = "";
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/mec/toolchain/v1/porting/" + projectId)
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isNotFound());
        Assert.assertEquals(result.andReturn().getResponse().getStatus(), 404);
        Assert.assertTrue(result.andReturn().getResponse().getContentAsString().equals(""));
    }

    @Test
    public void testGetSourceCodeFail2() throws Exception {

        String projectId = "123@^aaa";
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get("/mec/toolchain/v1/porting/" + projectId)
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(result.andReturn().getResponse().getStatus(), 200);
        Assert.assertTrue(result.andReturn().getResponse().getContentAsString().contains("false"));
    }

}
