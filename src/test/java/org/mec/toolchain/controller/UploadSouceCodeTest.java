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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

public class UploadSouceCodeTest extends PortingControllerTest {

    @Test
    public void testUploadSuccess() throws Exception {
        ClassPathResource resource = new ClassPathResource("testdata/72965ecc-47e8-44e3-88c2-f09269a6f61a.tgz");
        File sourceFile = resource.getFile();
        InputStream sourceInputStream = new FileInputStream(sourceFile);

        MultipartFile sourceMultiFile = new MockMultipartFile(sourceFile.getName(), sourceFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), sourceInputStream);

        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.multipart("/mec/toolchain/v1/porting/" + UUID.randomUUID().toString())
                .file("file", sourceMultiFile.getBytes()).contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                ).andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
        Assert.assertEquals(result.andReturn().getResponse().getStatus(), 200);

    }

    @Test
    public void testUploadFail() throws Exception {
        ClassPathResource resource = new ClassPathResource("testdata/72965ecc-47e8-44e3-88c2-f09269a6f61a.tgz");
        File sourceFile = resource.getFile();
        InputStream sourceInputStream = new FileInputStream(sourceFile);

        MultipartFile sourceMultiFile = new MockMultipartFile(sourceFile.getName(), sourceFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), sourceInputStream);

        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.multipart("/mec/toolchain/v1/porting/?").file("file", sourceMultiFile.getBytes())
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(MockMvcResultHandlers.print());
        Assert.assertEquals(result.andReturn().getResponse().getStatus(), 404);

    }
}
