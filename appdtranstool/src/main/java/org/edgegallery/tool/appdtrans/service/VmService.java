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

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.edgegallery.tool.appdtrans.constants.ResponseConst;
import org.edgegallery.tool.appdtrans.controller.dto.request.TransVmPkgReqDto;
import org.edgegallery.tool.appdtrans.exception.ToolException;
import org.edgegallery.tool.appdtrans.model.AppInfo;
import org.edgegallery.tool.appdtrans.model.AppPkgInfo;
import org.edgegallery.tool.appdtrans.model.ComputeInfo;
import org.edgegallery.tool.appdtrans.model.DefinitionInfo;
import org.edgegallery.tool.appdtrans.model.GenerateValueInfo;
import org.edgegallery.tool.appdtrans.model.RenameFileInfo;
import org.edgegallery.tool.appdtrans.model.ReplaceFileInfo;
import org.edgegallery.tool.appdtrans.model.RuleInfo;
import org.edgegallery.tool.appdtrans.model.UpdateFileInfo;
import org.edgegallery.tool.appdtrans.model.ZipFileInfo;
import org.edgegallery.tool.appdtrans.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("vmService")
public class VmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static final String DEFINE_PATH = "/vm/definitions";

    private static final String RULE_PATH = "/vm/rules";

    private static final String JSON_FILE_EXTENSION = ".json";

    private static final String APP_INFO_DEF = "appInfo";

    private static final String COMPUTE_INFO_DEF = "computeInfo";

    private static final String ZIP_EXTENSION = "zip";

    private static final String MF_EXTENSION = "mf";
    
    private static final String FILE_SEPARATOR = "/";

    @Autowired
    private LocalFileUtils localFileUtils;

    @Value("${transTool.tool-path}")
    private String toolHome;

    @Value("${transTool.appd-configs}")
    private String configDir;

    /**
     * get app package info.
     *
     * @param filePath app package full path
     * @param sourceAppd source appd
     * @return AppPkgInfo
     */
    public AppPkgInfo getAppPkgInfo(String filePath, String sourceAppd) {
        String defFilePath = configDir + DEFINE_PATH + FILE_SEPARATOR + sourceAppd + JSON_FILE_EXTENSION;
        File defFile = new File(defFilePath);
        try {
            String fileContent = FileUtils.readFileToString(defFile, StandardCharsets.UTF_8);
            JsonObject jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();
            JsonObject jsonAppInfo = jsonObject.get(APP_INFO_DEF).getAsJsonObject();
            JsonObject jsonComputeInfo = jsonObject.get(COMPUTE_INFO_DEF).getAsJsonObject();
            AppInfo appInfo = getAppInfo(filePath, jsonAppInfo);
            ComputeInfo computeInfo = getComputeInfo(filePath, jsonComputeInfo);
            return new AppPkgInfo(appInfo, computeInfo);

        } catch (IOException e) {
            throw new ToolException("failed to get package info from file.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
    }

    /**
     * get app info.
     *
     * @param filePath app package full path
     * @param jsonAppInfo app definition info
     * @return AppInfo
     */
    public AppInfo getAppInfo(String filePath, JsonObject jsonAppInfo) {
        String appName = getValueFromMfFile(filePath, jsonAppInfo.get("app_name").getAsJsonObject().toString());
        String appProvider = getValueFromMfFile(filePath, jsonAppInfo.get("app_provider").getAsJsonObject().toString());
        String appVersion = getValueFromMfFile(filePath,
            jsonAppInfo.get("app_package_version").getAsJsonObject().toString());
        String appTime = getValueFromMfFile(filePath,
            jsonAppInfo.get("app_release_data_time").getAsJsonObject().toString());
        String appType = getValueFromMfFile(filePath, jsonAppInfo.get("app_type").getAsJsonObject().toString());
        String appDesc = getValueFromMfFile(filePath,
            jsonAppInfo.get("app_package_description").getAsJsonObject().toString());
        return new AppInfo(appName, appProvider, appVersion, appTime, appType, appDesc);
    }

    /**
     * get compute info.
     *
     * @param filePath app package full path
     * @param jsonComputeInfo compute definition info
     * @return ComputeInfo
     */
    public ComputeInfo getComputeInfo(String filePath, JsonObject jsonComputeInfo) {
        String vmName = getValueFromYamlFile(filePath, jsonComputeInfo.get("vm_name").getAsJsonObject().toString());
        String storageSize = getValueFromYamlFile(filePath,
            jsonComputeInfo.get("storagesize").getAsJsonObject().toString());
        String memSize = getValueFromYamlFile(filePath, jsonComputeInfo.get("memorysize").getAsJsonObject().toString());
        String vcpu = getValueFromYamlFile(filePath, jsonComputeInfo.get("vcpu").getAsJsonObject().toString());
        String imageName = getValueFromYamlFile(filePath,
            jsonComputeInfo.get("image_name").getAsJsonObject().toString());
        return new ComputeInfo(vmName, storageSize, memSize, vcpu, imageName);
    }

    private String getValueFromMfFile(String filePath, String jsonInfo) {
        Gson g = new Gson();
        DefinitionInfo itemDef = g.fromJson(jsonInfo, new TypeToken<DefinitionInfo>() { }.getType());
        if (!itemDef.getFileType().equals(".mf")) {
            LOGGER.error("file type is wrong, not .mf.");
            return null;
        }
        try (ZipFile zipFile = new ZipFile(filePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                // check the path of entry if equal to ruled path
                if (!StringUtils.isEmpty(itemDef.getFilePath())) {
                    if (!fileName.contains(itemDef.getFilePath())
                        || !fileName.substring(0, fileName.lastIndexOf(FILE_SEPARATOR)).equals(itemDef.getFilePath())) {
                        continue;
                    }
                }
                // check the posix of file
                if (fileName.endsWith(itemDef.getFileType())) {
                    // check the file if in exclude file
                    if (!StringUtils.isEmpty(itemDef.getExcludeFile())
                        && fileName.substring(fileName.lastIndexOf("/") + 1).equals(itemDef.getExcludeFile())) {
                        continue;
                    }
                    try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            // prefix: path
                            if (line.trim().startsWith(itemDef.getLocation())) {
                                return line.split(":")[1].trim();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ToolException("failed to get value from mf file.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
        return null;
    }

    private String getValueFromYamlFile(String filePath, String jsonInfo) {
        Gson g = new Gson();
        DefinitionInfo itemDef = g.fromJson(jsonInfo, new TypeToken<DefinitionInfo>() { }.getType());
        if (!itemDef.getFileType().equals(".yaml")) {
            LOGGER.error("file type is wrong, not .yaml.");
            return null;
        }
        List<String> location = Arrays.asList(itemDef.getLocation().split("\\."));
        try (ZipFile zipFile = new ZipFile(filePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                // check the path of entry if equal to ruled path
                if (!StringUtils.isEmpty(itemDef.getFilePath())) {
                    if (!fileName.contains(itemDef.getFilePath())
                        || !fileName.substring(0, fileName.lastIndexOf(FILE_SEPARATOR)).equals(itemDef.getFilePath())) {
                        continue;
                    } else if (itemDef.isZip()
                        && Files.getFileExtension(fileName.toLowerCase()).equals(ZIP_EXTENSION)) {
                        File tempFile = File.createTempFile("tempFile", "zip");
                        FileOutputStream  tempOut = new FileOutputStream(tempFile);
                        IOUtils.copy(zipFile.getInputStream(new ZipEntry(fileName)), tempOut);
                        ZipFile innerZipFile = new ZipFile(tempFile);
                        return getValueFromInnerZip(innerZipFile, itemDef, location);
                    }
                }
                // check the posix of file
                if (fileName.endsWith(itemDef.getFileType())) {
                    // check the file if in exclude file
                    if (!StringUtils.isEmpty(itemDef.getExcludeFile())
                        && fileName.substring(fileName.lastIndexOf("/") + 1).equals(itemDef.getExcludeFile())) {
                        continue;
                    }

                    String yamlContent = getYamlContentFromZip(zipFile, entry);
                    Yaml yaml = new Yaml(new SafeConstructor());
                    Map<String, Object> loaded;
                    loaded = yaml.load(yamlContent);
                    return getObjectFromMap(loaded, location);
                }
            }
        } catch (IOException e) {
            throw new ToolException("failed to get value from yaml file.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
        return null;
    }

    private String getValueFromInnerZip(ZipFile innerZipFile, DefinitionInfo defInfo, List<String> location) {
        try {
            Enumeration<? extends ZipEntry> entries = innerZipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                // check the path of entry if equal to ruled path
                if (!StringUtils.isEmpty(defInfo.getSubPath())) {
                    if (!fileName.contains(defInfo.getSubPath())
                        || !fileName.substring(0, fileName.lastIndexOf(FILE_SEPARATOR)).equals(defInfo.getSubPath())) {
                        continue;
                    }
                }
                // check the posix of file
                if (fileName.endsWith(defInfo.getFileType())) {
                    // check the file if in exclude file
                    if (!StringUtils.isEmpty(defInfo.getExcludeFile())
                        && fileName.substring(fileName.lastIndexOf("/") + 1).equals(defInfo.getExcludeFile())) {
                        continue;
                    }

                    String yamlContent = getYamlContentFromZip(innerZipFile, entry);
                    Yaml yaml = new Yaml(new SafeConstructor());
                    Map<String, Object> loaded;
                    loaded = yaml.load(yamlContent);
                    return getObjectFromMap(loaded, location);
                }
            }
        } catch (IOException e) {
            throw new ToolException("failed to get value from yaml file.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
        return null;
    }

    private String getYamlContentFromZip(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
            File newYamlFile = new File("deploy.yaml");
            FileOutputStream out = new FileOutputStream(newYamlFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out,  StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line + "\r\n");
            }
            br.close();
            bw.close();

            String yamlContent = FileUtils.readFileToString(newYamlFile, StandardCharsets.UTF_8);
            yamlContent = yamlContent.replaceAll("\t", "");
            return yamlContent;
        }
    }

    /**
     * get Object From Map.
     *
     */
    private String getObjectFromMap(Map<String, Object> loaded, List<String> location) {
        LinkedHashMap<String, Object> result = null;
        for (int i = 0; i < location.size() - 1; i++) {
            result = (LinkedHashMap<String, Object>) loaded.get(location.get(i));
            if (result != null) {
                loaded = result;
            }
        }
        if (result != null) {
            return result.get(location.get(location.size() - 1)).toString();
        }
        return null;
    }

    /**
     * get rule info.
     *
     * @param destAppd dest appd
     * @return RuleInfo
     */
    public RuleInfo getRuleInfo(String destAppd) {
        String defFilePath = configDir + RULE_PATH + FILE_SEPARATOR + destAppd + JSON_FILE_EXTENSION;
        try {
            String fileContent = FileUtils.readFileToString(new File(defFilePath), StandardCharsets.UTF_8);
            Gson g = new Gson();
            RuleInfo ruleInfo = g.fromJson(fileContent, new TypeToken<RuleInfo>() { }.getType());
            return ruleInfo;
        } catch (IOException e) {
            throw new ToolException("failed to get package info from file.", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
    }

    /**
     * generate needed value.
     *
     */
    public void generateValue(GenerateValueInfo genInfo, AppPkgInfo appPkgInfo) {
        String value;
        if (genInfo.getType().equals("uuid")) {
            value = UUID.randomUUID().toString();
        } else {
            value = appPkgInfo.getAppInfo().getAppName() + "_" + appPkgInfo.getComputeInfo().getImageName();
        }
        switch (genInfo.getItem()) {
            case "image_id":
                appPkgInfo.setImageId(value);
                break;
            case "product_id":
                appPkgInfo.setProductId(value);
                break;
            case "vnfd_id":
                appPkgInfo.setVnfId(value);
                break;
            default:
                LOGGER.error("not supported value.");
        }
    }

    /**
     * replace files.
     *
     */
    public void replaceFiles(TransVmPkgReqDto dto, String parentDir, ReplaceFileInfo replaceFileInfo) {
        try {
            if (!StringUtils.isEmpty(dto.getDocFile()) && !StringUtils.isEmpty(replaceFileInfo.getDocFilePath())) {
                String srcDocFile = toolHome  + FILE_SEPARATOR + dto.getDocFile();
                String dstDocFile = parentDir + replaceFileInfo.getDocFilePath();
                FileUtils.copyFile(new File(srcDocFile), new File(dstDocFile));
            }

            if (!StringUtils.isEmpty(dto.getDeployFile())
                && !StringUtils.isEmpty(replaceFileInfo.getDeployFilePath())) {
                String srcDeployFile = toolHome + FILE_SEPARATOR + dto.getDeployFile();
                String dstDeployFile = parentDir + replaceFileInfo.getDeployFilePath();
                FileUtils.copyFile(new File(srcDeployFile), new File(dstDeployFile));
            }
        } catch (IOException e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_REPLACE_FILE_FAILED);
        }
    }

    /**
     * add image file to package.
     * @param imageFile image file
     * @param parentDir parent dir
     * @param imagePath image path
     * @return image path
     */
    public String addImageFileToPkg(String imageFile, String parentDir, String imagePath) {
        if (!StringUtils.isEmpty(imageFile)) {
            String imageFilePath = toolHome + FILE_SEPARATOR + imageFile;
            localFileUtils.fileCheck(imageFilePath);
            String imageDir = parentDir + FILE_SEPARATOR + "Image";
            File imgFile = new File(imageFilePath);
            try {
                localFileUtils.checkDir(new File(imageDir));
                FileUtils.copyToDirectory(imgFile, new File(imageDir));
            } catch (Exception e) {
                throw new ToolException(e.getMessage(), ResponseConst.RET_COPY_FILE_FAILED);
            }
            try (ZipFile zipFile = new ZipFile(imageFilePath)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        return "/Image/" + imgFile.getName() + FILE_SEPARATOR + entry.getName();
                    }
                }
            } catch (IOException e) {
                throw new ToolException("failed to get image path from image file.",
                    ResponseConst.RET_PARSE_FILE_EXCEPTION);
            }
        }
        return imagePath;
    }

    /**
     * build envs which be updated in files.
     *
     */
    public Map<String, String> buildUpdateVals(AppPkgInfo appPkgInfo, String imagePath, TransVmPkgReqDto dto) {
        Map<String, String> updVar2Values = new HashMap<String, String>();
        updVar2Values.put("{app_name}", appPkgInfo.getAppInfo().getAppName());
        updVar2Values.put("{app_provider}", appPkgInfo.getAppInfo().getAppProvider());
        updVar2Values.put("{app_package_version}", appPkgInfo.getAppInfo().getAppVersion());
        updVar2Values.put("{app_release_data_time}", appPkgInfo.getAppInfo().getAppReleaseTime());
        updVar2Values.put("{app_type}", appPkgInfo.getAppInfo().getAppType());
        updVar2Values.put("{app_package_description}", appPkgInfo.getAppInfo().getAppDesc());
        updVar2Values.put("{vm_name}", appPkgInfo.getComputeInfo().getVmname());
        updVar2Values.put("{storagesize}", appPkgInfo.getComputeInfo().getStorageSize());
        updVar2Values.put("{memorysize}", appPkgInfo.getComputeInfo().getMemorySize());
        updVar2Values.put("{vcpu}", appPkgInfo.getComputeInfo().getVcpu());
        updVar2Values.put("{image_name}", appPkgInfo.getComputeInfo().getImageName());
        updVar2Values.put("{vnfd_id}", appPkgInfo.getVnfId());
        updVar2Values.put("image_id", appPkgInfo.getImageId());
        updVar2Values.put("app_package_version", appPkgInfo.getAppInfo().getAppVersion());
        updVar2Values.put("image_name", appPkgInfo.getComputeInfo().getImageName());
        updVar2Values.put("{product_id}", appPkgInfo.getProductId());
        // inputs
        addInputValue(updVar2Values, "image_path", imagePath);
        addInputValue(updVar2Values, "{az}", dto.getAz());
        addInputValue(updVar2Values, "{flavor}", dto.getFlavor());
        addInputValue(updVar2Values, "{bootdata}", dto.getBootData());
        return updVar2Values;
    }

    private void addInputValue(Map<String, String> updVar2Values, String key, String value) {
        if (StringUtils.isEmpty(value)) {
            updVar2Values.put(key, "");
        } else {
            updVar2Values.put(key, value);
        }
    }

    /**
     * update files in package.
     */
    public void updateFiles(String dstFileDir, List<UpdateFileInfo> updateFileInfos,
        Map<String, String> env2Values) {
        try {
            for (UpdateFileInfo updateFileInfo : updateFileInfos) {
                File updFile = new File(dstFileDir + updateFileInfo.getFile());
                for (String env : updateFileInfo.getEnvs()) {
                    if (env2Values.get(env) != null) {
                        FileUtils.writeStringToFile(updFile,
                            FileUtils.readFileToString(updFile, StandardCharsets.UTF_8)
                                .replace(env, env2Values.get(env)),
                            StandardCharsets.UTF_8, false);
                    }
                }
            }
        } catch (IOException e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_UPD_FILE_FAILED);
        }
    }

    /**
     * rename files.
     */
    public void renameFiles(String dstFileDir, List<RenameFileInfo> renameFileInfos,
        Map<String, String> env2Values) {
        for (RenameFileInfo renameFileInfo : renameFileInfos) {
            String srcFileName = dstFileDir + renameFileInfo.getFile();
            File renameFile = new File(srcFileName);
            String newName = env2Values.get(renameFileInfo.getNewName());
            String newFileName = srcFileName.substring(0, srcFileName.lastIndexOf(FILE_SEPARATOR) + 1) + newName
                + "." + Files.getFileExtension(srcFileName.toLowerCase());
            if (renameFile.renameTo(new File(newFileName))) {
                LOGGER.info("rename {} to {}  success.", srcFileName, newName);
            }
        }
    }

    /**
     * zip files.
     */
    public void zipFiles(String dstFileDir, List<ZipFileInfo> zipFileInfos, Map<String, String> env2Values) {
        for (ZipFileInfo zipFileInfo: zipFileInfos) {
            String zipDir = dstFileDir + zipFileInfo.getPath();
            if (!org.springframework.util.StringUtils.isEmpty(zipDir)) {
                File dir = new File(zipDir);
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null && files.length > 0) {
                        List<File> subFiles = Arrays.asList(files);
                        if (!CollectionUtils.isEmpty(subFiles)) {
                            String zipFile = zipDir + FILE_SEPARATOR + env2Values.get(zipFileInfo.getZipName())
                                + ".zip";
                            localFileUtils.zipFiles(subFiles, new File(zipFile));
                            for (File subFile : subFiles) {
                                FileUtils.deleteQuietly(subFile);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * hash check.
     */
    public void hashCheck(String dstFileDir) {
        File mfFile = localFileUtils.getFile(dstFileDir, MF_EXTENSION);
        List<String> hashFilePaths = localFileUtils.getHashFilePaths(mfFile);
        for (int i = 0; i < hashFilePaths.size(); i++) {
            String fileName = dstFileDir + FILE_SEPARATOR + hashFilePaths.get(i);
            try (FileInputStream fis = new FileInputStream(fileName)) {
                String hashValue = DigestUtils.sha256Hex(fis);
                if (i == 0) {
                    FileUtils.writeStringToFile(mfFile,
                        FileUtils.readFileToString(mfFile, StandardCharsets.UTF_8).replace("{hash_first}", hashValue));
                } else {
                    FileUtils.writeStringToFile(mfFile,
                        FileUtils.readFileToString(mfFile, StandardCharsets.UTF_8).replace("{hash_second}", hashValue));
                }
            } catch (IOException e) {
                LOGGER.error("replace mf file hash value failed. {}", e.getMessage());
            }
        }
        // add image zip file hash check
        addImageCheck(dstFileDir, mfFile);
    }

    private void addImageCheck(String dstFileDir, File mfFile) {
        File imageFile = localFileUtils.getFile(dstFileDir + FILE_SEPARATOR + "Image", ZIP_EXTENSION);
        if (imageFile != null) {
            try {
                String imagePath = imageFile.getCanonicalPath();
                String hashFile = imagePath.substring(dstFileDir.length() + 1).replace(File.separator, FILE_SEPARATOR);
                String sourceData =  "Source: " + hashFile + "\n";
                FileUtils.writeStringToFile(mfFile, sourceData, StandardCharsets.UTF_8, true);
                FileUtils.writeStringToFile(mfFile, "Algorithm: SHA-256\n", StandardCharsets.UTF_8, true);
                try (FileInputStream fis = new FileInputStream(imagePath)) {
                    String hashValue = DigestUtils.sha256Hex(fis);
                    String hashData = "Hash: " + hashValue + "\n";
                    FileUtils.writeStringToFile(mfFile, hashData, StandardCharsets.UTF_8, true);
                } catch (IOException e) {
                    LOGGER.error("add image file hash check failed {}", e.getMessage());
                }

                // add image zip
                String toscaMeta = dstFileDir + "/TOSCA-Metadata/TOSCA.meta";
                File metaFile = new File(toscaMeta);
                if (metaFile.exists()) {
                    String contentName = "Name: " + hashFile + "\n";
                    FileUtils.writeStringToFile(metaFile, contentName, StandardCharsets.UTF_8, true);
                    FileUtils.writeStringToFile(metaFile, "Content-Type: image\n", StandardCharsets.UTF_8, true);
                }
            } catch (IOException e) {
                LOGGER.error("add image file hash check failed {}", e.getMessage());
            }
        }
    }

    /**
     * clear env.
     */
    public void clearEnv(TransVmPkgReqDto dto) {
        try {
            if (!StringUtils.isEmpty(dto.getAppFile())) {
                FileUtils.forceDelete(new File(toolHome + FILE_SEPARATOR + dto.getAppFile()));
            }
            if (!StringUtils.isEmpty(dto.getDocFile())) {
                FileUtils.forceDelete(new File(toolHome + FILE_SEPARATOR + dto.getDocFile()));
            }
            if (!StringUtils.isEmpty(dto.getImageFile())) {
                FileUtils.forceDelete(new File(toolHome + FILE_SEPARATOR + dto.getImageFile()));
            }
            if (!StringUtils.isEmpty(dto.getDeployFile())) {
                FileUtils.forceDelete(new File(toolHome + FILE_SEPARATOR + dto.getDeployFile()));
            }
        } catch (IOException e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_DEL_FILE_FAILED);
        }
    }
}
