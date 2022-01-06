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

package org.edgegallery.tool.appdtrans.constants;

public class ResponseConst {
    /**
     * the success code.
     */
    public static final int RET_SUCCESS = 0;

    /**
     * the fail code.
     */
    public static final int RET_FAIL = 1;

    /**
     * file size is too big.
     */
    public static final int RET_FILE_TOO_BIG = 100001;

    /**
     * too many files to unzip.
     */
    public static final int RET_UNZIP_TOO_MANY_FILES = 100002;

    /**
     * unzip file failed.
     */
    public static final int RET_UNZIP_FILE_FAILED = 100003;

    /**
     * file name postfix is invalid.
     */
    public static final int RET_FILE_NAME_POSTFIX_INVALID = 100004;

    /**
     * parse file failed.
     */
    public static final int RET_PARSE_FILE_EXCEPTION = 100005;

    /**
     * compress file failed.
     */
    public static final int RET_COMPRESS_FAILED = 100006;

    /**
     * make directory failed.
     */
    public static final int RET_MAKEDIR_FAILED = 100007;

    /**
     * copy file failed.
     */
    public static final int RET_COPY_FILE_FAILED = 100008;

    /**
     * update file failed.
     */
    public static final int RET_UPD_FILE_FAILED = 100009;

    /**
     * rename file failed.
     */
    public static final int RET_RENAME_FILE_FAILED = 100010;

    /**
     * file not found.
     */
    public static final int RET_FILE_NOT_FOUND = 100011;

    /**
     * upload file failed.
     */
    public static final int RET_UPLOAD_FILE_FAILED = 100012;

    /**
     * merge file failed.
     */
    public static final int RET_MERGE_FILE_FAILED = 100013;

    /**
     * transform package failed.
     */
    public static final int RET_TRANS_PKG_FAILED = 100014;

    /**
     * delete file failed.
     */
    public static final int RET_DEL_FILE_FAILED = 100015;

    /**
     * replace file failed.
     */
    public static final int RET_REPLACE_FILE_FAILED = 100016;

    /**
     * sign package failed.
     */
    public static final int RET_SIGN_PACKAGE_FAILED = 100017;

    private ResponseConst() {
        throw new IllegalStateException("Utility class");
    }
}
