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
        Resource test1 = resourceManager.createResource(TEST, null);
        Resource test2 = resourceManager.createResource(TEST, null);
        Resource test3 = resourceManager.createResource(TEST, null);
        test1.addDependency(test2);
        test2.addDependency(test3);
        test3.addDependency(test1);
        Assert.assertFalse(ctx.execute());
    }

    @Test
    public void testDependencyResolution() throws InvalidRefException, STRuntimeException, ResourceCreationException {
        Resource test1 = resourceManager.createResource(TEST);
        test1.setId("test1");
        Resource test2 = resourceManager.createResource(TEST);
        test2.setId("test2");
        Resource test3 = resourceManager.createResource(TEST);
        test3.setId("test3");
        test2.addDependency("id:test1");
        test3.addDependency("type:test:test");
        assertTrue(ctx.execute());
        assertEquals(test2.getResolvedDeps().size(), 1);
        assertEquals(test2.getResolvedDeps().get(0), test1);
        assertEquals(test3.getResolvedDeps().size(), 2);
        assertTrue(test3.getResolvedDeps().contains(test1));
        assertTrue(test3.getResolvedDeps().contains(test2));
    }

    @Test(expectedExceptions = InvalidDependencyException.class, expectedExceptionsMessageRegExp = "Unable to find.*")
    public void testNoMatchesMandatoryDependencies() throws STRuntimeException, InvalidRefException, ResourceCreationException {
        Resource resource = resourceManager.createResource("test");
        resource.addDependency("type:tes");
        assertTrue(ctx.execute());
    }

    @Test
    public void testNoMatchesOptionalDependencies() throws STRuntimeException, InvalidRefException, ResourceCreationException {
        Resource resource = resourceManager.createResource("test");
        resource.addDependency("type:tes", true);
        assertTrue(ctx.execute());
        assertTrue(resource.getResolvedDeps().isEmpty());
    }
}
