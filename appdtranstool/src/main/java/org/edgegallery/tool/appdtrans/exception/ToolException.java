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

package org.edgegallery.tool.appdtrans.exception;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.tool.appdtrans.controller.dto.response.ErrorMessage;

public class ToolException extends RuntimeException {

    private static final long serialVersionUID = 2311101258952411166L;

    private ErrorMessage errMsg;

    /**
     * Constructor to create ToolException with message.
     *
     * @param msg exception message
     */
    public ToolException(String msg) {
        super(msg);
    }

    /**
     * Constructor to create ToolException with retCode and params.
     *
     * @param ret retCode
     */
    public ToolException(String msg, int ret) {
        super(msg);
        ErrorMessage errorMessage = new ErrorMessage(ret, null);
        errMsg = errorMessage;
    }

    /**
     * Constructor to create ToolException with retCode and params.
     *
     * @param ret retCode
     * @param args params of error message
     */
    public ToolException(String msg, int ret, Object... args) {
        super(msg);
        List<String> params = new ArrayList<>();
        int length = args == null ? 0 : args.length;
        for (int i = 0; i < length; i++) {
            params.add(args[i].toString());
        }
        ErrorMessage errorMessage = new ErrorMessage(ret, params);
        errMsg = errorMessage;
    }

    /**
     * get error message.
     *
     */
    public ErrorMessage getErrMsg() {
        return errMsg;
    }
}

