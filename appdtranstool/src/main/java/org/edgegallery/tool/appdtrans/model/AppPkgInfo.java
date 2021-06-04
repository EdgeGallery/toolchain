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

package org.edgegallery.tool.appdtrans.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppPkgInfo {
    private AppInfo appInfo;

    private ComputeInfo computeInfo;

    private String imageId;

    private String vnfId;

    private String productId;

    /**
     * Constructor of AppPkgInfo.
     */
    public AppPkgInfo(AppInfo appInfo, ComputeInfo computeInfo) {
        this.appInfo = appInfo;
        this.computeInfo = computeInfo;
    }
}
