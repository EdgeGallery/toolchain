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
