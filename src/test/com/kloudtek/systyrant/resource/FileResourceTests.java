/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.TempDir;
import org.apache.commons.io.FileUtils;
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
    public static final byte[] DATA_SHA = CryptoUtils.sha1(DATA.getBytes());
    public static final byte[] OTHER_SHA = CryptoUtils.sha1("notthesamedata".getBytes());
    private Resource dir;
    private TempDir tempdir;

    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException, IOException, ScriptException {
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
        assertEquals(FileUtils.readFileToString(expected), "hello");
    }
}
