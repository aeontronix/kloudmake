/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidDependencyException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResourceDependencyRefTest extends AbstractContextTest {
    @Test(expectedExceptions = InvalidDependencyException.class)
    public void testCircularDependency() throws ResourceCreationException, STRuntimeException {
        Resource test1 = resourceManager.createResource(JTEST);
        Resource test2 = resourceManager.createResource(JTEST);
        Resource test3 = resourceManager.createResource(JTEST);
        test1.addDependency(test2);
        test2.addDependency(test3);
        test3.addDependency(test1);
        Assert.assertFalse(ctx.execute());
    }

    @Test
    public void testDependencyResolution() throws InvalidRefException, STRuntimeException, ResourceCreationException {
        Resource test1 = createJavaTestResource();
        test1.setId("test1");
        Resource test2 = createJavaTestResource();
        test2.setId("test2");
        Resource test3 = createJavaTestResource();
        test3.setId("test3");
        test2.addDependency("@id eq test1");
        test3.addDependency("type test:jtest");
        assertTrue(ctx.execute());
        assertEquals(test2.getDependencies().size(), 1);
        assertEquals(test2.getDependencies().iterator().next(), test1);
        assertEquals(test3.getDependencies().size(), 2);
        assertTrue(test3.getDependencies().contains(test1));
        assertTrue(test3.getDependencies().contains(test2));
    }

    @Test(expectedExceptions = InvalidDependencyException.class, expectedExceptionsMessageRegExp = "Mandatory dependency has not valid targets.*")
    public void testNoMatchesMandatoryDependencies() throws STRuntimeException, InvalidRefException, ResourceCreationException {
        Resource resource = createJavaTestResource();
        resource.addDependency("type te:tes");
        assertTrue(ctx.execute());
    }

    @Test
    public void testNoMatchesOptionalDependencies() throws STRuntimeException, InvalidRefException, ResourceCreationException {
        Resource resource = createJavaTestResource();
        resource.addDependency("type t:tes", true);
        assertTrue(ctx.execute());
        assertTrue(resource.getDependencies().isEmpty());
    }
}
