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

import static org.mockito.Matchers.eq;


import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;
import org.mec.toolchain.model.porting.BaseTask;
import org.mec.toolchain.model.porting.TaskDataDetail;
import org.mec.toolchain.model.porting.TaskResponse;
import org.mec.toolchain.model.porting.TaskResponseData;
import org.mec.toolchain.model.porting.TaskStatus;
import org.mec.toolchain.service.PortingService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

public class CreateTaskTest extends PortingControllerTest {

    @Test
    public void testCreateTaskSuccess() throws Exception {
        String projectId = UUID.randomUUID().toString();
        String taskId = "202020202020";
        BaseTask baseTask = new BaseTask();
        baseTask.setSourceDir("/sourceDir");

        String tasksUrl = portingParamConfig.getTasksUrl();
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setStatus(0);
        taskResponse.setInfo("create task success");
        TaskResponseData taskData = new TaskResponseData();
        taskData.setId(taskId);
        taskResponse.setData(taskData);
        Mockito.when(httpUtil.httpsPost(eq(tasksUrl), Mockito.anyMap(), Mockito.anyString()))
            .thenReturn(gson.toJson(taskResponse));

        String getTaskStatusUrl = portingParamConfig.getTasksUrl() + taskId + "/";
        TaskStatus taskStatus = new TaskStatus();
        TaskDataDetail data = new TaskDataDetail();
        data.setStatus(2);
        taskStatus.setData(data);
        Mockito.when(httpUtil.httpsGet(eq(getTaskStatusUrl), Mockito.anyMap())).thenReturn(gson.toJson(taskStatus));

        new MockUp<PortingService>() {
            @Mock
            private void moveReport(String reportId, String projectId) {
                return;
            }
        };

        mvc.perform(MockMvcRequestBuilders.post("/mec/toolchain/v1/porting/" + projectId + "/tasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8).content(gson.toJson(baseTask)))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void testCreateTaskFail() throws Exception {
        String projectId = UUID.randomUUID().toString();
        BaseTask baseTask = new BaseTask();
        baseTask.setSourceDir("/sourceDir");

        String tasksUrl = portingParamConfig.getTasksUrl();
        Mockito.when(httpUtil.httpsPost(eq(tasksUrl), Mockito.anyMap(), Mockito.anyString())).thenReturn(null);

        mvc.perform(MockMvcRequestBuilders.post("/mec/toolchain/v1/porting/" + projectId + "/tasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8).content(gson.toJson(baseTask)));
    }

    @Test(expected = NestedServletException.class)
    public void testCreateTaskFai2() throws Exception {
        String projectId = UUID.randomUUID().toString();
        BaseTask baseTask = new BaseTask();
        baseTask.setSourceDir("/sourceDir");

        String tasksUrl = portingParamConfig.getTasksUrl();
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setStatus(1);
        taskResponse.setInfo("create task fail");
        Mockito.when(httpUtil.httpsPost(eq(tasksUrl), Mockito.anyMap(), Mockito.anyString()))
            .thenReturn(gson.toJson(taskResponse));

        mvc.perform(MockMvcRequestBuilders.post("/mec/toolchain/v1/porting/" + projectId + "/tasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8).content(gson.toJson(baseTask)));
    }

    @Test(expected = NestedServletException.class)
    public void testCreateTaskFai3() throws Exception {
        String projectId = UUID.randomUUID().toString();
        String taskId = null;
        BaseTask baseTask = new BaseTask();
        baseTask.setSourceDir("/sourceDir");

        String tasksUrl = portingParamConfig.getTasksUrl();
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setStatus(0);
        taskResponse.setInfo("create task success");
        TaskResponseData taskData = new TaskResponseData();
        taskData.setId(taskId);
        taskResponse.setData(taskData);
        Mockito.when(httpUtil.httpsPost(eq(tasksUrl), Mockito.anyMap(), Mockito.anyString()))
            .thenReturn(gson.toJson(taskResponse));

        mvc.perform(MockMvcRequestBuilders.post("/mec/toolchain/v1/porting/" + projectId + "/tasks")
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId)
            .accept(MediaType.APPLICATION_JSON_UTF8).content(gson.toJson(baseTask)));
    }
}
