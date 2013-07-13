/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.AbstractContextTest;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class FileFragmentTests extends AbstractContextTest {
    private static final AtomicInteger counter = new AtomicInteger();
    private File tempFile;
    private String path;

    @BeforeMethod
    public void createTmpFile() throws IOException {
        tempFile = File.createTempFile("filefragtest", "tmp");
        path = tempFile.getPath();
        tempFile.delete();
    }

    @AfterMethod
    public void delTmpFile() {
        if (!tempFile.delete()) {
            tempFile.deleteOnExit();
        }
    }

    @Test
    public void testCreateXmlFileFromFragments() throws Throwable {
        createXmlFragment("grangrandchild", "root.child.grandchild", "@bla = 'ble'", "hello='world'");
        createXmlFragment("grandchild", "root.child", "@hello = 'world'", "bla='ble'");
        createXmlFragment("grandchild", "root.child", "@foo = 'bar'", null);
        createXmlFragment("child", "root", null, "hello='world'");
        createXmlFragment("child", "root", null, "foo='bar'");
        createXmlFragment("root", null, null, null);
        createFile();
        execute();
        String xml = FileUtils.readFileToString(tempFile);
        System.out.println();
    }

    @Test
    public void testMultipleConflictingTypesOnFile() {
        // TODO
    }

    @Test
    public void testTwoFileWithDifferentFragmentTypes() {
        // TODO
    }

    private Resource createFile() throws ResourceCreationException, InvalidAttributeException {
        Resource resource = ctx.getResourceManager().createResource("core.file");
        resource.set("path", path);
        resource.set("content", "<test>\n\t<testchild/>\n</test>");
        return resource;
    }

    private Resource createXmlFragment(String type, String parent, String xpath, String attributes) throws ResourceCreationException, InvalidAttributeException {
        StringBuilder tmp = new StringBuilder();
        if (parent != null) {
            tmp.append(parent).append(".");
        }
        tmp.append(type).append('#').append(counter.incrementAndGet());
        Resource resource = ctx.getResourceManager().createResource("core.xmlfile", tmp.toString());
        resource.set("path", path);
        resource.set("type", type);
        resource.set("parent", parent);
        resource.set("xpath", xpath);
        resource.set("attributes", attributes);
        return resource;
    }

}
