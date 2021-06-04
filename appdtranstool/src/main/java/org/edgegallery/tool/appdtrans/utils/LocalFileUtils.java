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

package org.edgegallery.tool.appdtrans.utils;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.tool.appdtrans.constants.ResponseConst;
import org.edgegallery.tool.appdtrans.exception.ToolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("localFileUtils")
public class LocalFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileUtils.class);

    static final int TOO_MANY = 1024;

    static final Long TOO_BIG = 5 * 1024 * 1024 * 1024L;

    private static final String ZIP_EXTENSION = ".zip";

    private static final String ZIP_PACKAGE_ERR_MESSAGES = "failed to compress file";

    /**
     * file check.
     *
     * @param fileFullName file full name
     */
    public void fileCheck(String fileFullName) {
        String filePosx = Files.getFileExtension(fileFullName.toLowerCase());
        if (filePosx.equals(".zip") || filePosx.equals(".csar")) {
            unzipPacakge(fileFullName);
        } else {
            throw new ToolException("file extension is invalid.", ResponseConst.RET_FILE_NAME_POSTFIX_INVALID);
        }
    }

    /**
     * zip bomb check.
     *
     * @param localFilePath file full name
     */
    private void unzipPacakge(String localFilePath) {
        String parentDir = localFilePath.substring(0, localFilePath.lastIndexOf(File.separator));
        try (ZipFile zipFile = new ZipFile(localFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int entriesCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entriesCount > TOO_MANY) {
                    throw new ToolException("too many files to unzip",
                        ResponseConst.RET_UNZIP_TOO_MANY_FILES, TOO_MANY);
                }
                entriesCount++;
                // sanitize file path
                String fileName = sanitizeFileName(entry.getName(), parentDir);
                if (!entry.isDirectory()) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        if (inputStream.available() > TOO_BIG) {
                            throw new ToolException("file being unzipped is too big",
                                ResponseConst.RET_FILE_TOO_BIG, TOO_BIG);
                        }
                        FileUtils.copyInputStreamToFile(inputStream, new File(fileName));
                        LOGGER.info("unzip package... {}", entry.getName());
                    }
                } else {
                    File dir = new File(fileName);
                    boolean dirStatus = dir.mkdirs();
                    LOGGER.debug("creating dir {}, status {}", fileName, dirStatus);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to unzip");
            throw new ToolException("Failed to unzip", ResponseConst.RET_UNZIP_FILE_FAILED);
        }
    }

    /**
     *sanitize file name.
     *
     * @param entryName entry name.
     * @parm  intendedDir parent dir
     */
    public String sanitizeFileName(String entryName, String intendedDir) throws IOException {
        File f = new File(intendedDir, entryName);
        String canonicalPath = f.getCanonicalPath();
        File intendDir = new File(intendedDir);
        if (intendDir.isDirectory() && !intendDir.exists()) {
            createFile(intendedDir);
        }
        String canonicalID = intendDir.getCanonicalPath();
        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath;
        } else {
            throw new IllegalStateException("file is outside extraction target directory.");
        }
    }

    /**
     *create file name.
     *
     * @param filePath file name.
     */
    private void createFile(String filePath) throws IOException {
        File tempFile = new File(filePath);
        boolean result = false;

        if (!tempFile.getParentFile().exists() && !tempFile.isDirectory()) {
            result = tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists() && !tempFile.isDirectory() && !tempFile.createNewFile() && !result) {
            throw new IllegalArgumentException("create temp file failed");
        }
    }

    /**
     * ZIP application package.
     *
     * @param intendedDir application package ID
     */
    public String compressAppPackage(String intendedDir) throws IOException {
        final Path srcDir = Paths.get(intendedDir);
        String zipFileName = intendedDir.concat(ZIP_EXTENSION);
        File tempFile = new File(zipFileName);
        if (tempFile.exists()) {
            FileUtils.forceDelete(tempFile);
        }
        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            java.nio.file.Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = srcDir.relativize(file);
                        os.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = java.nio.file.Files.readAllBytes(file);
                        os.write(bytes, 0, bytes.length);
                        os.closeEntry();
                    } catch (IOException e) {
                        throw new ToolException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new ToolException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        return zipFileName;
    }

    /**
     * zip files.
     * @param srcfile source file list
     * @param zipfile to be zipped file
     */
    public void zipFiles(List<File> srcfile, File zipfile) {
        List<String> entryPaths = new ArrayList<>();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));) {
            for (File file : srcfile) {
                if (file.isFile()) {
                    addFileToZip(out, file, entryPaths);
                } else if (file.isDirectory()) {
                    entryPaths.add(file.getName());
                    addFolderToZip(out, file, entryPaths);
                    entryPaths.remove(entryPaths.size() - 1);
                }
            }
        } catch (IOException e) {
            throw new ToolException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
    }

    private static void addFolderToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/") + "/"));
        out.closeEntry();
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File subFile : files) {
            if (subFile.isFile()) {
                addFileToZip(out, subFile, entryPaths);
            } else if (subFile.isDirectory()) {
                entryPaths.add(subFile.getName());
                addFolderToZip(out, subFile, entryPaths);
                entryPaths.remove(entryPaths.size() - 1);
            }
        }
    }

    private static void addFileToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        byte[] buf = new byte[1024];
        try (FileInputStream in = new FileInputStream(file)) {
            if (entryPaths.size() > 0) {
                out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/") + "/" + file.getName()));
            } else {
                out.putNextEntry(new ZipEntry(file.getName()));
            }
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        }
    }

    /**
     * check dir exist and make dir.
     * @param fileDir file dir
     */
    public void checkDir(File fileDir) {
        try {
            if (!fileDir.exists() && !fileDir.mkdirs()) {
                throw new ToolException("failed to create directory.", ResponseConst.RET_MAKEDIR_FAILED);
            }
        } catch (Exception e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_MAKEDIR_FAILED);
        }
    }

    /**
     * get source file path.
     *
     * @param mfFile mf File
     */
    public List<String> getHashFilePaths(File mfFile) {
        List<String> hashFilePaths = new ArrayList<>();
        try (BufferedReader br =
                 new BufferedReader(new InputStreamReader(FileUtils.openInputStream(mfFile), StandardCharsets.UTF_8))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] splitByColon = line.split(":");
                // Source: path
                if (splitByColon.length > 1 && "Source".equals((splitByColon[0]))) {
                    hashFilePaths.add(splitByColon[1].trim());
                }
            }
        } catch (IOException e) {
            LOGGER.error("get file hash failed.{}", e.getMessage());
        }
        return hashFilePaths;
    }

    /**
     * get file by parent directory and file extension.
     */
    public File getFile(String parentDir, String fileExtension) {
        try {
            List<File> files = (List<File>) FileUtils.listFiles(new File(parentDir), null, true);
            for (File fileEntry : files) {
                if (Files.getFileExtension(fileEntry.getName().toLowerCase()).equals(fileExtension)) {
                    return fileEntry;
                }
            }
        } catch (Exception e) {
            throw new ToolException(e.getMessage(), ResponseConst.RET_FILE_NOT_FOUND);
        }
        return null;
    }
}
