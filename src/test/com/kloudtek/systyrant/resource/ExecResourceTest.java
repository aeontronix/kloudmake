/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractContextTest;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class ExecResourceTest extends AbstractContextTest {
    @Test
    public void simpleExecTest() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfFailed() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep F " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfSuccess() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep X " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestUnlessFailed() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' unless='grep F " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestUnlessSuccess() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' unless='grep X " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfNegatedByUnless() throws Throwable {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runScript("core.exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep X " + tempFile.getPath() + "' unless='grep X " + tempFile.getPath() + "'}");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTimeout() throws Throwable {
        ctx.clearFatalException();
        ctx.runScript("core.exec { command = 'sleep 3' timeout='1'}");
        execute(false);
    }
}
