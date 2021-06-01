/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.tool.appdtrans.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.edgegallery.tool.appdtrans.controller.dto.request.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("vmService")
public class VmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static final String TEMPLATES_PATH = "/configs/vm/templates";

    @Value("${appdtranstool-be.home-path}")
    private String appHome;

    @Value("${appdtranstool-be.temp-path}")
    private String fileTempPath;

    /**
     * get appd templates types.
     *
     */
    public List<String> getVmTemplates() {
        List<String> templates = new ArrayList<>();
        File file = new File(TEMPLATES_PATH);
        File[] lstFiles = file.listFiles();
        for (File filePath : lstFiles) {
            if (filePath.isDirectory()) {
                templates.add(filePath.getName());
            }
        }
        return templates;
    }

    /**
     * upload image.
     */
    public ResponseEntity<String> uploadImage(boolean isMultipart, Chunk chunk) throws IOException {
        if (isMultipart) {
            MultipartFile file = chunk.getFile();

            if (file == null) {
                LOGGER.error("can not find any needed file");
                return ResponseEntity.badRequest().build();
            }
            File uploadDirTmp = new File(fileTempPath);
            checkDir(uploadDirTmp);

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                chunkNumber = 0;
            }
            File outFile = new File(fileTempPath + File.separator + chunk.getIdentifier(), chunkNumber + ".part");
            try (InputStream inputStream = file.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, outFile);
            }
        }

        return ResponseEntity.ok("upload package block success.");
    }

    /**
     * merge image.
     */
    public ResponseEntity merge(String fileName, String guid, String fileType) throws IOException {
        File uploadDir = new File(appHome);
        checkDir(uploadDir);
        File file = new File(fileTempPath + File.separator + guid);
        String randomPath = "";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                String newFileAddress = appHome + File.separator + fileType;
                File partFiles = new File(newFileAddress);
                checkDir(partFiles);
                randomPath = fileType + File.separator + fileName;
                String newFileName = partFiles + File.separator + fileName;
                File partFile = new File(newFileName);
                for (int i = 1; i <= files.length; i++) {
                    File s = new File(fileTempPath + File.separator + guid, i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(file);
            }
        }

        return ResponseEntity.ok(randomPath);
    }

    private void checkDir(File fileDir) throws IOException {
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            throw new IOException("create folder failed");
        }
    }
}
