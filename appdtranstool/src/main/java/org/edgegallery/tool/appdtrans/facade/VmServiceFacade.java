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

package org.edgegallery.tool.appdtrans.facade;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.edgegallery.tool.appdtrans.constants.ResponseConst;
import org.edgegallery.tool.appdtrans.controller.dto.request.Chunk;
import org.edgegallery.tool.appdtrans.controller.dto.request.TransVmPkgReqDto;
import org.edgegallery.tool.appdtrans.controller.dto.response.ErrorMessage;
import org.edgegallery.tool.appdtrans.controller.dto.response.ResponseObject;
import org.edgegallery.tool.appdtrans.exception.IllegalRequestException;
import org.edgegallery.tool.appdtrans.exception.ToolException;
import org.edgegallery.tool.appdtrans.model.AppPkgInfo;
import org.edgegallery.tool.appdtrans.model.GenerateValueInfo;
import org.edgegallery.tool.appdtrans.model.RuleInfo;
import org.edgegallery.tool.appdtrans.service.VmService;
import org.edgegallery.tool.appdtrans.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("vmServiceFacade")
public class VmServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmServiceFacade.class);

    private static final String TEMPLATES_PATH = "/vm/templates/";

    private static final String FILE_SEPARATOR = "/";

    @Autowired
    private VmService vmService;

    @Autowired
    private LocalFileUtils localFileUtils;

    @Value("${transTool.tool-path}")
    private String toolHome;

    @Value("${transTool.upload-path}")
    private String fileTempPath;

    @Value("${transTool.appd-configs}")
    private String configDir;

    @Value("${transTool.encrypted-key-path:}")
    private String keyPath;

    @Value("${transTool.key-password:}")
    private String keyPwd;

    /**
     * get appd templates types.
     *
     */
    public List<String> getVmTemplates() {
        List<String> templates = new ArrayList<>();
        File file = new File(configDir + TEMPLATES_PATH);
        File[] lstFiles = file.listFiles();
        if (lstFiles == null) {
            return null;
        }
        for (File filePath : lstFiles) {
            if (filePath.isDirectory()) {
                templates.add(filePath.getName());
            }
        }
        return templates;
    }

    /**
     * upload file.
     */
    public ResponseEntity<ResponseObject> uploadFile(boolean isMultipart, Chunk chunk) {
        try {
            if (isMultipart) {
                MultipartFile file = chunk.getFile();
                if (file == null) {
                    LOGGER.error("can not find any needed file");
                    throw new IllegalRequestException("can not find any needed file", ResponseConst.RET_FILE_NOT_FOUND);
                }
                File uploadDirTmp = new File(fileTempPath);
                localFileUtils.checkDir(uploadDirTmp);

                Integer chunkNumber = chunk.getChunkNumber();
                if (chunkNumber == null) {
                    chunkNumber = 0;
                }
                File outFile = new File(fileTempPath + FILE_SEPARATOR + chunk.getIdentifier(), chunkNumber + ".part");
                try (InputStream inputStream = file.getInputStream()) {
                    FileUtils.copyInputStreamToFile(inputStream, outFile);
                }
            }
            ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
            return ResponseEntity
                .ok(new ResponseObject("upload file block success.", errMsg, "upload file block success."));
        } catch (IOException e) {
            throw new ToolException("upload file block exception.", ResponseConst.RET_UPLOAD_FILE_FAILED);
        }
    }

    /**
     * merge file.
     */
    public ResponseEntity<ResponseObject> merge(String fileName, String guid, String fileType) {
        try {
            File uploadDir = new File(toolHome);
            localFileUtils.checkDir(uploadDir);
            File file = new File(fileTempPath + FILE_SEPARATOR + guid);
            String randomPath = "";
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    String newFileAddress = toolHome + FILE_SEPARATOR + fileType;
                    File partFiles = new File(newFileAddress);
                    localFileUtils.checkDir(partFiles);
                    randomPath = fileType + FILE_SEPARATOR + fileName;
                    String newFileName = partFiles + FILE_SEPARATOR + fileName;
                    File partFile = new File(newFileName);
                    for (int i = 1; i <= files.length; i++) {
                        File s = new File(fileTempPath + FILE_SEPARATOR + guid, i + ".part");
                        FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                        FileUtils.copyFile(s, destTempfos);
                        destTempfos.close();
                    }
                    FileUtils.deleteDirectory(file);
                }
            }
            ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
            return ResponseEntity
                .ok(new ResponseObject(randomPath, errMsg, "merge file success."));
        } catch (IOException e) {
            throw new ToolException("merge file exception.", ResponseConst.RET_MERGE_FILE_FAILED);
        }
    }

    /**
     * transform app package.
     *
     */
    public ResponseEntity<InputStreamResource> transVmPackage(TransVmPkgReqDto dto) {
        try {
            // 1. zip bomb check and get package main info
            String appSourcePkgFile = toolHome + FILE_SEPARATOR + dto.getAppFile();
            localFileUtils.fileCheck(appSourcePkgFile);
            AppPkgInfo appPkgInfo = vmService.getAppPkgInfo(appSourcePkgFile, dto.getSourceAppd());

            // 2. copy dest template to specified path
            String dstFileDir = toolHome + FILE_SEPARATOR + dto.getDestAppd();
            FileUtils.deleteDirectory(new File(dstFileDir));
            localFileUtils.checkDir(new File(toolHome));
            FileUtils.copyToDirectory(new File(configDir + TEMPLATES_PATH + dto.getDestAppd()), new File(toolHome));

            // 3. generate values
            RuleInfo ruleInfo = vmService.getRuleInfo(dto.getDestAppd());
            for (GenerateValueInfo genInfo : ruleInfo.getGenerateValues()) {
                vmService.generateValue(genInfo, appPkgInfo);
            }

            // 4. replace file
            vmService.replaceFiles(dto, dstFileDir, ruleInfo.getReplaceFiles());

            // 5. add image file
            String imagePath = vmService.addImageFileToPkg(dto.getImageFile(), dstFileDir, dto.getImagePath());

            // 6. update file
            Map<String, String> updVar2Values = vmService.buildUpdateVals(appPkgInfo, imagePath, dto);
            vmService.updateFiles(dstFileDir, ruleInfo.getUpdateFiles(), updVar2Values);

            // 7. rename file
            vmService.renameFiles(dstFileDir, ruleInfo.getRenameFiles(), updVar2Values);

            // 8. zip file
            vmService.zipFiles(dstFileDir, ruleInfo.getZipFiles(), updVar2Values);

            // 9. hash check
            vmService.hashCheck(dstFileDir);

            // 10. sign package
            localFileUtils.signPackage(dstFileDir, keyPath, keyPwd);

            // 11. zip all package
            String dstPkgFile = localFileUtils.compressAppPackage(dstFileDir, appPkgInfo.getAppInfo().getAppName());

            // 12. clear env
            vmService.clearEnv(dto);

            // 13. return zip package
            InputStream ins = new BufferedInputStream(new FileInputStream(dstPkgFile));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/octet-stream");
            headers.add("Content-Disposition", "attachment; filename="
                + appPkgInfo.getAppInfo().getAppName());
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
        } catch (IOException e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_TRANS_PKG_FAILED);
        }
    }
}
