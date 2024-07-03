/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource;

import com.aeontronix.aeonbuild.AbstractContextTest;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.exception.InvalidAttributeException;
import com.aeontronix.aeonbuild.exception.ResourceCreationException;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.xml.XPathUtils;
import com.kloudtek.util.xml.XmlUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class FileFragmentTests extends AbstractContextTest {
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
        createFile(path);
        createXmlFragment(path, "insert", "/test", "<testinsert/>");
        createXmlFragment(path, "replace", "/test/testchild", "<testreplace/>");
        createXmlFragment(path, "delete", "/test/deleteme", null);
        execute();
        String xmlStr = FileUtils.toString(tempFile);
        System.out.println(xmlStr);
        Document xmlDoc = XmlUtils.parse(new StringReader(xmlStr));
        assertNotNull(XPathUtils.evalXPathElement("/test/testinsert", xmlDoc));
        assertNotNull(XPathUtils.evalXPathElement("/test/testreplace", xmlDoc));
        assertNull(XPathUtils.evalXPathElement("/test/deleteme", xmlDoc));
    }


    @Test
    public void testTwoXmlFiles() throws Throwable {
        createFile(path);
        createXmlFragment(path, "insert", "/test", "<testinsert1/>");
        File tempFile2 = File.createTempFile("filefragtest", "tmp");
        String path2 = tempFile2.getPath();
        try {
            createFile(path2);
            createXmlFragment(path2, "insert", "/test", "<testinsert2/>");
        } finally {
            tempFile2.delete();
        }
        execute();
        String xmlStr = FileUtils.toString(this.tempFile);
        System.out.println(xmlStr);
        Document xmlDoc = XmlUtils.parse(new StringReader(xmlStr));
        assertNotNull(XPathUtils.evalXPathElement("/test/testinsert1", xmlDoc));
        assertNull(XPathUtils.evalXPathElement("/test/testinsert2", xmlDoc));
        xmlStr = FileUtils.toString(tempFile2);
        System.out.println(xmlStr);
        xmlDoc = XmlUtils.parse(new StringReader(xmlStr));
        assertNotNull(XPathUtils.evalXPathElement("/test/testinsert2", xmlDoc));
        assertNull(XPathUtils.evalXPathElement("/test/testinsert1", xmlDoc));
    }

    @Test
    public void testMultipleConflictingTypesOnFile() {
    }

    @Test
    public void testTwoFileWithDifferentFragmentTypes() throws IOException, SAXException, XPathExpressionException, ResourceCreationException, InvalidAttributeException {
    }

    @Test
    public void testTwoFilesSameNamesDifferentHosts() throws IOException, SAXException, XPathExpressionException, ResourceCreationException, InvalidAttributeException {
    }

    private Resource createFile(String path) throws ResourceCreationException, InvalidAttributeException {
        Resource resource = ctx.getResourceManager().createResource("core.file");
        resource.set("path", path);
        resource.set("content", "<test>\n<testchild/>\n<deleteme/></test>");
        return resource;
    }

    private Resource createXmlFragment(String path, String type, String xpath, String xml) throws ResourceCreationException, InvalidAttributeException {
        Resource resource = ctx.getResourceManager().createResource("core.xmlfile");
        resource.set("path", path);
        resource.set("type", type);
        resource.set("xpath", xpath);
        resource.set("xml", xml);
        return resource;
    }

}
