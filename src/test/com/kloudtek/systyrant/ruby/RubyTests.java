/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.ruby;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class RubyTests extends AbstractContextTest {
    @Test
    public void runRubyScript() throws Throwable {
        ctx.runScript(getClass().getResource("runruby.rb"));
        List<Resource> res = resourceManager.getResources();
        execute();
        Assert.assertEquals(res.size(), 2);
        Assert.assertEquals(res.get(0).getType().toString(), "test:test");
        Assert.assertEquals(res.get(1).getType().toString(), "test:test");
        Assert.assertEquals(res.get(1).getId(), "myres");
        Assert.assertEquals(res.get(1).get("key1"), "val1");
        Assert.assertEquals(res.get(1).get("key2"), "val2");
        Assert.assertEquals(res.get(1).get("key3"), "val3");
        Assert.assertEquals(res.get(1).get("key4"), "val4");
    }
}
