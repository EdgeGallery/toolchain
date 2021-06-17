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
import java.io.FileNotFoundException;
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

    private static final String FILE_SEPARATOR = "/";

    /**
     * file check.
     *
     * @param fileFullName file full name
     */
    public void fileCheck(String fileFullName) {
        String filePosix = Files.getFileExtension(fileFullName.toLowerCase());
        try {
            if (filePosix.equals("zip") || filePosix.equals("csar")) {
                unzipPacakge(fileFullName);
            } else {
                throw new ToolException("file extension is invalid.", ResponseConst.RET_FILE_NAME_POSTFIX_INVALID);
            }
        } catch (IOException e) {
            throw new ToolException("delete temp directory failed.", ResponseConst.RET_DEL_FILE_FAILED);
        }
    }

    /**
     * zip bomb check.
     *
     * @param localFilePath file full name
     */
    private void unzipPacakge(String localFilePath) throws IOException {
        String tempDir = localFilePath.substring(0, localFilePath.lastIndexOf(FILE_SEPARATOR) + 1) + "temp";
        checkDir(new File(tempDir));
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
                String fileName = sanitizeFileName(entry.getName(), tempDir);
                File f = new File(fileName);
                if (isDir(entry, f)) {
                    continue;
                }
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    if (inputStream.available() > TOO_BIG) {
                        throw new ToolException("file being unzipped is too big",
                            ResponseConst.RET_FILE_TOO_BIG, TOO_BIG);
                    }
                    FileUtils.copyInputStreamToFile(inputStream, f);
                    LOGGER.info("unzip package... {}", entry.getName());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to unzip");
            throw new ToolException("Failed to unzip", ResponseConst.RET_UNZIP_FILE_FAILED);
        } finally {
            FileUtils.deleteDirectory(new File(tempDir));
        }
    }

    /**
     *sanitize file name.
     *
     * @param entryName entry name.
     * @parm  intendedDir parent dir
     */
    private String sanitizeFileName(String entryName, String intendedDir) throws IOException {
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
     * check if entry is directory, if then create dir.
     *
     * @param entry entry of next element.
     * @param f File
     */
    private boolean isDir(ZipEntry entry, File f) {
        if (entry.isDirectory()) {
            boolean isSuccess = f.mkdirs();
            if (isSuccess) {
                return true;
            } else {
                return f.exists();
            }
        }
        return false;
    }

    /**
     * ZIP application package.
     *
     * @param intendedDir application package ID
     */
    public String compressAppPackage(String intendedDir, String zipName) {
        final Path srcDir = Paths.get(intendedDir);
        String zipFileName = zipName + ZIP_EXTENSION;
        String[] fileName = zipFileName.split(FILE_SEPARATOR);
        String fileStorageAdd = srcDir + FILE_SEPARATOR + fileName[fileName.length - 1];
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            createCompressedFile(out, new File(intendedDir), "");
        } catch (IOException e) {
            throw new ToolException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        try {
            FileUtils.deleteDirectory(new File(intendedDir));
            FileUtils.moveFileToDirectory(new File(zipFileName), new File(intendedDir), true);
        } catch (IOException e) {
            throw new ToolException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        return fileStorageAdd;
    }

    private void createCompressedFile(ZipOutputStream out, File file, String dir) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (!dir.equals("")) {
                out.putNextEntry(new ZipEntry(dir + FILE_SEPARATOR));
            }

            dir = dir.length() == 0 ? "" : dir + FILE_SEPARATOR;
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    createCompressedFile(out, files[i], dir + files[i].getName());
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                out.putNextEntry(new ZipEntry(dir));
                int j = 0;
                byte[] buffer = new byte[1024];
                while ((j = fis.read(buffer)) > 0) {
                    out.write(buffer, 0, j);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("createCompressedFile: can not find param file, {}", e.getMessage());
                throw new ToolException("can not find file", ResponseConst.RET_COMPRESS_FAILED);
            }
        }
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
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            throw new ToolException("failed to create directory.", ResponseConst.RET_MAKEDIR_FAILED);
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
        List<File> files = (List<File>) FileUtils.listFiles(new File(parentDir), null, true);
        for (File fileEntry : files) {
            if (Files.getFileExtension(fileEntry.getName().toLowerCase()).equals(fileExtension)) {
                return fileEntry;
            }
        }
        return null;
    }
}
