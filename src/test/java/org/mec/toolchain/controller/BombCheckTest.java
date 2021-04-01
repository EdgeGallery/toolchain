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

package org.mec.toolchain.controller;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.mec.toolchain.util.PackageChecker;
import org.springframework.core.io.ClassPathResource;

public class BombCheckTest {

    @Test
    public void should_successful_when_read_gz_file() throws IOException {
        PackageChecker checker = new PackageChecker();
        ClassPathResource resource = new ClassPathResource("testdata/72965ecc-47e8-44e3-88c2-f09269a6f61a.tgz");
        File sourceFile = resource.getFile();
        boolean check = checker.bombCheckGzip(sourceFile);
        Assert.assertTrue(check);
    }
}
