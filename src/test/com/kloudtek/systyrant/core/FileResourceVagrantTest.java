/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.core;

import com.kloudtek.systyrant.AbstractVagrantTest;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.testng.annotations.Test;

import javax.script.ScriptException;
import java.io.IOException;

public class FileResourceVagrantTest extends AbstractVagrantTest {
    @Test
    public void testCreateNonExistingFile() throws STRuntimeException, IOException, ScriptException {
        ctx.runDSLScript("new core:file { path='/root/testfile', content='hello', owner='uucp', group='fuse'}");
        execute();
    }
}
