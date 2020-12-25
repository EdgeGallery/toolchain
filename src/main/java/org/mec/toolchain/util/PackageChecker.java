/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.mec.toolchain.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageChecker {

    static final int BUFFER = 512;

    static final int TOOBIG = 0x640000; // max size of unzipped data, 100MB

    static final int TOOMANY = 1024; // max number of files

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageChecker.class);

    private final String sendbox = "./sendbox/";

    /**
     * Constructor to create PackageChecker.
     */
    public PackageChecker() {
        File sendboxDir = new File(sendbox);
        if (sendboxDir.exists() && sendboxDir.isDirectory()) {
            return;
        }
        try {
            org.mec.toolchain.util.FileUtil.createNewDir(sendbox);
        } catch (IOException e) {
            LOGGER.error("failed to create sendbox {}.", sendbox);
        }

    }

    /**
     * Returns dir.
     *
     * @return dir
     */
    public String getSendbox() {
        return sendbox;
    }

    private String sanitzeFileName(String entryName, String intendedDir) throws IOException {

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
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    protected void createFile(String filePath) throws IOException {
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
     * Prevent bomb attacks.
     *
     * @param gzFile file.
     * @throws IOException throw IOException
     */
    public final boolean bombCheckGzip(File gzFile) {
        int entries = 0;
        int total = 0;
        byte[] data = new byte[BUFFER];
        try (InputStream is = FileUtils.openInputStream(gzFile);
             CompressorInputStream in = new GzipCompressorInputStream(is, true);
             TarArchiveInputStream tin = new TarArchiveInputStream(in)) {
            ArchiveEntry entry;
            while ((entry = tin.getNextEntry()) != null) {
                int count;
                // Write the files to the disk, but ensure that the entryName is valid,
                // and that the file is not insanely big
                String name = sanitzeFileName(entry.getName(), getSendbox() + File.separator + "temp");
                File f = new File(name);
                if (isDir(entry, f)) {
                    continue;
                }
                FileOutputStream fos = FileUtils.openOutputStream(f);
                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                    while (total <= TOOBIG && (count = tin.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                        total += count;
                    }
                    dest.flush();
                }
                // tin.close();
                entries++;
                if (entries > TOOMANY) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                if (total > TOOBIG) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("failed to check zip bomb.");
            return false;
        } finally {
            // delete send box contents
            FileUtil.deleteContents(new File(getSendbox()));
        }
    }

    /**
     * check if entry is directory, if then create dir.
     *
     * @param entry entry of next element.
     * @param f File
     * @return
     */
    private boolean isDir(ArchiveEntry entry, File f) {
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
}
