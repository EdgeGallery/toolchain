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

import com.spencerwi.either.Either;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.mec.toolchain.model.dto.FormatRespDto;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    /**
     * build response by either.
     *
     * @param either either
     * @param <T> clazz
     * @return response entity
     */
    public static <T> ResponseEntity<T> buildResponse(Either<FormatRespDto, T> either) {
        if (either.isLeft()) {
            throw new InvocationException(either.getLeft().getEnumStatus().getStatusCode(),
                either.getLeft().getEnumStatus().getReasonPhrase(), either.getLeft().getErrorRespDto());
        }
        return ResponseEntity.ok(either.getRight());
    }

}
