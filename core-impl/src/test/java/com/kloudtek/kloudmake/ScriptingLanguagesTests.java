/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake;

import org.testng.annotations.Test;

/**
 * Tests the use of scripting languages to create resources
 */
public class ScriptingLanguagesTests extends AbstractContextTest {
    @Test
    public void testRubyResourceCreation() throws Throwable {
        execute("$strm.createResource( 'test.test' )", "rb");
        assertResources("test.test:test.test1");
    }

    @Test
    public void testRubyResourceCreationWithAttributes() throws Throwable {
        execute("$strm.createResource( 'test.test', 'myid' ).set( {'tkey' => 'tval'} )", "rb");
        assertResources("test.test:myid");
        assertResourceAttrs("test.test:myid", "tkey", "tval");
    }

    @Test
    public void testRubyResourceCreationWithAttributesUsingHelper() throws Throwable {
        execute("Kloudmake.create( 'test.test', 'myid', {'tkey' => 'tval'} )", "rb");
        assertResources("test.test:myid");
        assertResourceAttrs("test.test:myid", "tkey", "tval");
    }
}
