/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource;

import com.aeontronix.aeonbuild.AbstractContextTest;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.exception.InvalidResourceDefinitionException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.kloudtek.kryptotek.DigestUtils;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.TempDir;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class FileResourceTests extends AbstractContextTest {
    public static final String PATH = "/somewhere";
    public static final String PATH2 = "/somewherelse";
    public static final String DATA = "testdata";
    public static final byte[] DATA_SHA = DigestUtils.sha1(DATA.getBytes());
    public static final byte[] OTHER_SHA = DigestUtils.sha1("notthesamedata".getBytes());
    private Resource dir;
    private TempDir tempdir;

    @BeforeMethod
    public void init() throws KMRuntimeException, InvalidResourceDefinitionException, IOException, ScriptException {
        tempdir = new TempDir("frt");
        super.init();
    }

    @AfterMethod
    public void cleanup() throws IOException {
        tempdir.close();
    }

    @Test
    public void testCreateFileUsingContent() throws Throwable {
        File expected = new File(tempdir, "testfile.txt");
        ctx.getResourceManager().createResource("core.file").set("path", expected.getPath()).set("content", "hello");
        execute();
        assertEquals(FileUtils.toString(expected), "hello");
    }
}
