/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.InvalidDependencyException;
import com.aeontronix.aeonbuild.exception.InvalidRefException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ResourceDependencyRefTest extends AbstractContextTest {
    @Test(expectedExceptions = InvalidDependencyException.class)
    public void testCircularDependency() throws KMRuntimeException {
        Resource test1 = resourceManager.createResource(JTEST);
        Resource test2 = resourceManager.createResource(JTEST);
        Resource test3 = resourceManager.createResource(JTEST);
        test1.addDependency(test2);
        test2.addDependency(test3);
        test3.addDependency(test1);
        Assert.assertFalse(ctx.execute());
    }

    @Test
    public void testDependencyResolution() throws InvalidRefException, KMRuntimeException {
        Resource test1 = createJavaTestResource("test1");
        Resource test2 = createJavaTestResource("test2");
        Resource test3 = createJavaTestResource("test3");
        test2.addDependency("@id eq test1");
        test3.addDependency("type test.jtest");
        assertTrue(ctx.execute());
        assertEquals(test2.getDependencies().size(), 1);
        assertEquals(test2.getDependencies().iterator().next(), test1);
        assertEquals(test3.getDependencies().size(), 2);
        assertTrue(test3.getDependencies().contains(test1));
        assertTrue(test3.getDependencies().contains(test2));
    }

    @Test(expectedExceptions = InvalidDependencyException.class, expectedExceptionsMessageRegExp = "Mandatory dependency has not valid targets.*")
    public void testNoMatchesMandatoryDependencies() throws KMRuntimeException, InvalidRefException {
        Resource resource = createJavaTestResource();
        resource.addDependency("type te.tes");
        assertTrue(ctx.execute());
    }

    @Test
    public void testNoMatchesOptionalDependencies() throws KMRuntimeException, InvalidRefException {
        Resource resource = createJavaTestResource();
        resource.addDependency("type t.tes", true);
        assertTrue(ctx.execute());
        assertTrue(resource.getDependencies().isEmpty());
    }
}
