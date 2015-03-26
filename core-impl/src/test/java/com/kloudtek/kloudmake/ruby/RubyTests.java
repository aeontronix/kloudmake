/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.ruby;

import com.kloudtek.kloudmake.AbstractContextTest;
import com.kloudtek.kloudmake.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class RubyTests extends AbstractContextTest {
    @Test
    public void runRubyScript() throws Throwable {
        ctx.runScriptFile(getClass().getResource("runruby.rb"));
        List<Resource> resources = resourceManager.getResources();
        execute();
        Assert.assertEquals(resources.size(), 1);
        Resource res = resources.get(0);
        Assert.assertEquals(res.getType().toString(), "test.test");
        Assert.assertEquals(res.getId(), "myres");
        Assert.assertEquals(res.get("key1"), "val1");
        Assert.assertEquals(res.get("key2"), "val2");
        Assert.assertEquals(res.get("key3"), "val3");
        Assert.assertEquals(res.get("key4"), "val4");
    }
}
