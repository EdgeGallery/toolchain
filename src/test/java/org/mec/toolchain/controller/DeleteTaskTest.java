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
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.NestedServletException;

public class DeleteTaskTest extends PortingControllerTest {

    @Test(expected = NestedServletException.class)
    public void testDeleteTaskFail() throws Exception {
        String projectId = UUID.randomUUID().toString();
        String taskId = "10";

        mvc.perform(MockMvcRequestBuilders.delete("/mec/toolchain/v1/porting/" + projectId + "/tasks/" + taskId)
            .contentType(MediaType.APPLICATION_JSON_UTF8).param("projectId", projectId).param("id", taskId)
            .accept(MediaType.APPLICATION_JSON_UTF8));
    }
}
