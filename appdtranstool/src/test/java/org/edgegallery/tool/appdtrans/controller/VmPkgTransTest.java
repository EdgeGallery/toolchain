/* Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.tool.appdtrans.controller;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.tool.appdtrans.constants.ResponseConst;
import org.edgegallery.tool.appdtrans.controller.dto.request.Chunk;
import org.edgegallery.tool.appdtrans.controller.dto.request.TransVmPkgReqDto;
import org.edgegallery.tool.appdtrans.controller.dto.response.ResponseObject;
import org.edgegallery.tool.appdtrans.exception.IllegalRequestException;
import org.edgegallery.tool.appdtrans.exception.RestReturn;
import org.edgegallery.tool.appdtrans.exception.ToolException;
import org.edgegallery.tool.appdtrans.facade.VmServiceFacade;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppPkgTransToolTest.class)
@AutoConfigureMockMvc
public class VmPkgTransTest {

    @Autowired
    protected MockMvc mvc;

    protected Gson gson = new Gson();

    protected static final String PACKAGE1 = "testfile/package/OpenStackAPPV100.zip";

    protected static final String PACKAGE2 = "testfile/package/vmTest.csar";

    protected static final String DOC_FILE = "testfile/doc/template.md";

    protected static final String DEPLOY_FILE = "testfile/deploy/AR_APP.yaml";

    protected static final String IMAGE_FILE = "testfile/Image/cirros.zip";

    @Autowired
    private VmServiceFacade vmServiceFacade;

    private String pkgPath1;

    private String pkgPath2;

    private String docPath;

    private String deployPath;

    private String imagePath;

    @Value("${transTool.tool-path}")
    private String toolHome;

    @Value("${transTool.upload-path}")
    private String fileTempPath;

    @Value("${transTool.appd-configs}")
    private String configDir;

    @Before
    public void setUp() throws IOException {
        init();
    }

    @After
    public void after() throws IOException {
        FileUtils.deleteDirectory(new File(fileTempPath));
        FileUtils.deleteDirectory(new File(toolHome));
        FileUtils.deleteDirectory(new File(configDir));
        FileUtils.deleteDirectory(new File("usr/app/keys"));
    }

    private void init() throws IOException {
        // upload1
        File pkgFile1 = Resources.getResourceAsFile(PACKAGE1);
        FileInputStream fileInputStream = new FileInputStream(pkgFile1);
        MultipartFile multipartFile = new MockMultipartFile("file", pkgFile1.getName(), "text/plain", IOUtils.toByteArray(fileInputStream));
        Chunk chunk = new Chunk();
        chunk.setFile(multipartFile);
        chunk.setChunkSize(8 * 1024 * 1024L);
        chunk.setTotalSize(multipartFile.getSize());
        chunk.setIdentifier("12692-OpenStackAPPV100zip");
        upload(chunk);

        // merge1
        ResponseEntity<ResponseObject> result = vmServiceFacade.merge(pkgFile1.getName(), chunk.getIdentifier(), "package");
        ResponseObject response = result.getBody();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getData());
        pkgPath1 = response.getData().toString();

        // upload2
        File pkgFile2 = Resources.getResourceAsFile(PACKAGE2);
        FileInputStream fileInputStream2 = new FileInputStream(pkgFile2);
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", pkgFile2.getName(), "text/plain", IOUtils.toByteArray(fileInputStream2));
        Chunk chunk2 = new Chunk();
        chunk2.setFile(multipartFile2);
        chunk2.setChunkSize(8 * 1024 * 1024L);
        chunk2.setTotalSize(multipartFile2.getSize());
        chunk2.setIdentifier("10189-vmTestcsar");
        upload(chunk2);

        // merge2
        result = vmServiceFacade.merge(pkgFile2.getName(), chunk2.getIdentifier(), "package");
        response = result.getBody();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getData());
        pkgPath2 = response.getData().toString();

        // upload3
        File docFile = Resources.getResourceAsFile(DOC_FILE);
        FileInputStream fileInputStream3 = new FileInputStream(docFile);
        MockMultipartFile multipartFile3 = new MockMultipartFile("file", docFile.getName(), "text/plain", IOUtils.toByteArray(fileInputStream3));
        Chunk chunk3 = new Chunk();
        chunk3.setFile(multipartFile3);
        chunk3.setChunkSize(8 * 1024 * 1024L);
        chunk3.setTotalSize(multipartFile3.getSize());
        chunk3.setIdentifier("6766-templatemd");
        upload(chunk3);

        // merge3
        result = vmServiceFacade.merge(docFile.getName(), chunk3.getIdentifier(), "doc");
        response = result.getBody();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getData());
        docPath = response.getData().toString();

        // upload4
        File deployFile = Resources.getResourceAsFile(DEPLOY_FILE);
        FileInputStream fileInputStream4 = new FileInputStream(docFile);
        MockMultipartFile multipartFile4 = new MockMultipartFile("file", deployFile.getName(), "text/plain", IOUtils.toByteArray(fileInputStream4));
        Chunk chunk4 = new Chunk();
        chunk4.setFile(multipartFile4);
        chunk4.setChunkSize(8 * 1024 * 1024L);
        chunk4.setTotalSize(multipartFile4.getSize());
        chunk4.setIdentifier("10027-AR_APPyaml");
        upload(chunk4);

        // merge4
        result = vmServiceFacade.merge(deployFile.getName(), chunk4.getIdentifier(), "deploy");
        response = result.getBody();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getData());
        deployPath = response.getData().toString();

        // upload5
        File imgFile = Resources.getResourceAsFile(IMAGE_FILE);
        FileInputStream fileInputStream5 = new FileInputStream(imgFile);
        MockMultipartFile multipartFile5 = new MockMultipartFile("file", imgFile.getName(), "text/plain", IOUtils.toByteArray(fileInputStream5));
        Chunk chunk5 = new Chunk();
        chunk5.setFile(multipartFile5);
        chunk5.setChunkSize(8 * 1024 * 1024L);
        chunk5.setTotalSize(multipartFile5.getSize());
        chunk5.setIdentifier("15748944-cirroszip");
        upload(chunk5);

        // merge5
        result = vmServiceFacade.merge(imgFile.getName(), chunk5.getIdentifier(), "Image");
        response = result.getBody();
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getData());
        imagePath = response.getData().toString();

        // copy config file
        File srcConfigFile = Resources.getResourceAsFile("testfile/configs");
        File dstConfigFile = new File(configDir);
        FileUtils.copyDirectory(srcConfigFile, dstConfigFile);

        // copy key file
        File srcKeyFile = Resources.getResourceAsFile("testfile/keys");
        File dstKeyFile = new File("usr/app/keys");
        FileUtils.copyDirectory(srcKeyFile, dstKeyFile);
    }

    private void upload(Chunk chunk) {
        BigDecimal a = new BigDecimal(chunk.getTotalSize());
        BigDecimal b = new BigDecimal(chunk.getChunkSize());
        int blocks = (int) Math.ceil(a.divide(b).doubleValue());
        chunk.setTotalChunks(blocks);

        int i = 1;
        while (blocks > 0) {
            chunk.setChunkNumber(i);
            long currentSize = chunk.getChunkSize();
            if (blocks == 1) {
                long size = chunk.getTotalSize() % chunk.getChunkSize();
                currentSize = size == 0 ? currentSize : size;
            }
            chunk.setCurrentChunkSize(currentSize);
            vmServiceFacade.uploadFile(true, chunk);
            i++;
            blocks--;
        }
    }

    @Test
    public void should_get_templates_success() throws Exception {
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
            .get("/mec/appdtranstool/v1/vm/templates")
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void should_trans_eg_success() throws Exception {
        TransVmPkgReqDto dto = new TransVmPkgReqDto();
        dto.setSourceAppd("ChinaUnicom");
        dto.setDestAppd("EdgeGallery");
        dto.setAz("az1.dc1");
        dto.setFlavor("mgmt_egx86: true");
        dto.setBootData("#!/bin/bash\n  rm -rf /home/mep.ca  rm -rf /home/init.txt");

        dto.setAppFile(pkgPath1);
        dto.setDocFile(docPath);
        dto.setDeployFile(deployPath);
        dto.setImageFile(imagePath);

        MvcResult transResult = mvc.perform(MockMvcRequestBuilders
            .post("/mec/appdtranstool/v1/vm/trans")
            .content(gson.toJson(dto))
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), transResult.getResponse().getStatus());
    }

    @Test
    public void should_trans_unicom_success() throws Exception {
        TransVmPkgReqDto dto = new TransVmPkgReqDto();
        dto.setSourceAppd("EdgeGallery");
        dto.setDestAppd("ChinaUnicom");
        dto.setAppFile(pkgPath2);

        MvcResult transResult = mvc.perform(MockMvcRequestBuilders
            .post("/mec/appdtranstool/v1/vm/trans")
            .content(gson.toJson(dto))
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), transResult.getResponse().getStatus());
    }

    @Test(expected = IllegalRequestException.class)
    public void test_exception() {
        ToolException e = new ToolException("test ToolException.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        RestReturn restReturn = RestReturn.builder().code(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase()).message(e.getMessage())
            .path("/vm/test/excption").retCode(ResponseConst.RET_FAIL).params(null).build();
        Assert.assertEquals("test ToolException.", restReturn.getMessage());
        throw new IllegalRequestException("test IllegalRequestException.", ResponseConst.RET_FILE_NOT_FOUND);
    }
}
