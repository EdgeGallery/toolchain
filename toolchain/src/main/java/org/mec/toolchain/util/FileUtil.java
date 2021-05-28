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

package org.mec.toolchain.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final int LINE_MAX_LEN = 4096;

    /**
     * delete directory.
     *
     * @param file directory
     * @throws IOException io exception
     */
    public static void deleteDir(File file) throws IOException {
        if (file.exists() && file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        }
    }

    /**
     * create new directory.
     *
     * @param path dir path
     * @return directory
     * @throws IOException io exception
     */
    public static File createNewDir(String path) throws IOException {
        File file = new File(path);
        deleteDir(file);
        boolean res = file.mkdirs();
        if (res) {
            return file;
        }
        throw new IOException("Create directory exception");
    }

    /**
     * decompress .tar.gz to dest directory.
     *
     * @param destDir dest directory
     * @param tgz .tar.gz file
     * @throws IOException io exception
     */
    public static void deCompressTgz(String destDir, File tgz) throws IOException {
        try (InputStream is = FileUtils.openInputStream(tgz);
             CompressorInputStream in = new GzipCompressorInputStream(is, true);
             TarArchiveInputStream tin = new TarArchiveInputStream(in);) {
            TarArchiveEntry entry = tin.getNextTarEntry();
            while (entry != null) {
                File archiveEntry = new File(destDir, entry.getName());
                boolean res = archiveEntry.getParentFile().mkdirs();
                if (res && !archiveEntry.getParentFile().exists()) {
                    LOGGER.error("Create source directory fail");
                }
                if (entry.isDirectory()) {
                    boolean resu = archiveEntry.mkdir();
                    if (!resu) {
                        throw new IOException("Create directory exception");
                    }
                    entry = tin.getNextTarEntry();
                    continue;
                }
                try (OutputStream out = FileUtils.openOutputStream(archiveEntry)) {
                    IOUtils.copy(tin, out);
                }
                entry = tin.getNextTarEntry();
            }

        }
    }

    /**
     * copy directory.
     *
     * @param sourceDir sourceDir
     * @param targetDir targetDir
     * @throws IOException IOException
     */
    public static void copyDirToDir(File sourceDir, File targetDir) throws IOException {
        FileUtils.copyDirectoryToDirectory(sourceDir, targetDir);
    }

    /**
     * rede file to string.
     *
     * @param file file
     * @return string
     * @throws IOException IOException
     */
    public static String readFileToString(File file) throws IOException {
        try (FileInputStream in = FileUtils.openInputStream(file);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader buffReader = new BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();
            String str = "";
            while ((str = readLine(buffReader)) != null) {
                sb.append(str).append("\n");
            }
            return sb.toString();
        }
    }

    private static String readLine(BufferedReader br) throws IOException {
        return read(br, LINE_MAX_LEN);
    }

    private static String read(BufferedReader br, int maxLenOfLine) throws IOException {
        int intC = br.read();
        if (-1 == intC) {
            return null;
        }
        StringBuilder sb = new StringBuilder(10);
        while (intC != -1) {
            char c = (char) intC;
            if (c == '\n') {
                break;
            }
            if (sb.length() >= maxLenOfLine) {
                throw new IOException("line too long");
            }
            sb.append(c);
            intC = br.read();
        }
        String str = sb.toString();
        if (!str.isEmpty() && str.endsWith("\r")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
}
