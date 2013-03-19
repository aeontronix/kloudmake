/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.cli.Cli;
import org.testng.annotations.Test;

public class E2ETests {
    @Test
    public void testE2E() {
        Cli.main(new String[]{"src/test/com/kloudtek/systyrant/e2e.stl"});
    }
}
