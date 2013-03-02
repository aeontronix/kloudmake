/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class ExecResourceTest extends AbstractContextTest {
    @Test
    public void simpleExecTest() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfFailed() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep F " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfSuccess() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep X " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestUnlessFailed() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' unless='grep F " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "HELLO");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestUnlessSuccess() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' unless='grep X " + tempFile.getPath() + "' }");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void simpleExecTestIfNegatedByUnless() throws IOException, ScriptException, STRuntimeException {
        File tempFile = File.createTempFile("sec", "tmp");
        try {
            FileUtils.write(tempFile, "X");
            ctx.runDSLScript("new core:exec { command = 'sed -i -e s/X/HELLO/ " + tempFile.getPath() + "' if='grep X " + tempFile.getPath() + "' unless='grep X " + tempFile.getPath() + "'}");
            execute();
            assertEquals(FileUtils.readFileToString(tempFile).trim(), "X");
        } finally {
            tempFile.delete();
        }
    }
}
