/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceRef;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResourceRefTest extends AbstractContextTest {
    @Test
    public void testMatchByUId() throws STRuntimeException, InvalidAttributeException, InvalidRefException, ResourceCreationException {
        Resource e1 = resourceManager.createResource("file");
        e1.setUid("afile1");
        resourceManager.createResource("file").setUid("afile2");
        resolve("uid:afile1", e1);
    }

    @Test
    public void testMatchById() throws STRuntimeException, InvalidAttributeException, InvalidRefException, ResourceCreationException {
        Resource e1 = resourceManager.createResource("file");
        e1.setId("afile1");
        resourceManager.createResource("file").setId("afile2");
        resolve("id:afile1", e1);
    }

    @Test
    public void testMatchByExprEq() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "vaL1");
        final Resource e2 = createTestElement("value", "vAl1");
        createTestElement("value", "notme");
        resourceManager.createResource("test");
        resolve("expr: attr('value') eq 'vaL1'", e1);
    }

    @Test
    public void testMatchByAttrEscaping() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "/\\£\"|op");
        final Resource e2 = createTestElement("value", "hello||");
        final Resource e4 = createTestResource();
        resolve("attr:value eq /\\\\£\"\\|op|attr:value eq hello\\|\\|", e1, e2);
    }

    @Test(expectedExceptions = InvalidRefException.class)
    public void testMultipleMatchesInSingleSearch() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "hello");
        final Resource e2 = createTestElement("value", "hello");
        ResourceRef ref1 = new ResourceRef("attr: value eq hello");
        ref1.resolveSingle(ctx, null);
    }

    @Test
    public void testGreaterThanNumber() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "10");
        final Resource e2 = createTestElement("value", "20");
        resolve("attr: value > 15", e2);
    }

    @Test
    public void testGreaterThanOrEqualsNumber() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "10");
        final Resource e2 = createTestElement("value", "20");
        final Resource e3 = createTestElement("value", "30");
        resolve("attr: value >= 20", e2, e3);
    }

    @Test
    public void testLessThanNumber() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "10");
        final Resource e2 = createTestElement("value", "20");
        resolve("attr: value < 15", e1);
    }

    @Test
    public void testLessThanOrEqualsNumber() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "10");
        final Resource e2 = createTestElement("value", "20");
        final Resource e3 = createTestElement("value", "30");
        resolve("attr: value <= 20", e1, e2);
    }

    @Test
    public void testLessThanString() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "a");
        final Resource e2 = createTestElement("value", "c");
        resolve("attr: value < b", e1);
    }

    @Test
    public void testMoreThanString() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "a");
        final Resource e2 = createTestElement("value", "c");
        resolve("attr: value > b", e2);
    }

    @Test
    public void testIsNull() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        createTestElement("value", "a");
        final Resource e2 = createTestResource();
        resolve("attr: value isnull true", e2);
    }

    @Test
    public void testIsNotNull() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "a");
        createTestResource();
        resolve("attr: value isnull false", e1);
    }

    @Test
    public void testRegex() throws InvalidRefException, STRuntimeException, InvalidAttributeException, ResourceCreationException {
        final Resource e1 = createTestElement("value", "lo");
        final Resource e2 = createTestElement("value", "hello");
        final Resource e3 = createTestElement("value", "xxxx");
        resolve("attr: value regex lo", e1, e2);
    }

//    @Test
//    public void testAssignRefToElement() throws STRuntimeException, InvalidAttributeException, InvalidRefException, ResourceCreationException {
//        final Resource e1 = createTestResource("id", "test1");
//        final Resource e2 = createTestResource("id", "test2");
//        final Resource e3 = createTestResource("ref", "id:test1");
//        assertSame(TestResource.get(e3).getRef().resolveSingle(ctx, e3), e1);
//        assertEquals(e3.get("ref"), "id:test1");
//    }

    private void resolve(String value, Resource... expected) throws InvalidRefException {
        ResourceRef ref = new ResourceRef(value);
        List<Resource> found = ref.resolveMultiple(ctx, null);
        assertEquals(found.size(), expected.length);
        for (Resource resource : expected) {
            assertTrue(found.contains(resource));
        }
    }
}
