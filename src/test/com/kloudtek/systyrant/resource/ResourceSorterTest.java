/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidDependencyException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceSorterTest {
    @Test
    public void testSortSuccessful() throws Exception {
        Resource seven = new Resource(null, null, null, null, null);
        Resource five = new Resource(null, null, null, null, null);
        Resource three = new Resource(null, null, null, null, null);
        Resource eleven = new Resource(null, null, null, null, null);
        Resource eight = new Resource(null, null, null, null, null);
        Resource two = new Resource(null, null, null, null, null);
        Resource nine = new Resource(null, null, null, null, null);
        Resource ten = new Resource(null, null, null, null, null);
        seven.dependencies.add(eleven);
        seven.dependencies.add(eight);
        five.dependencies.add(eleven);
        three.dependencies.add(eight);
        three.dependencies.add(ten);
        eleven.dependencies.add(two);
        eleven.dependencies.add(nine);
        eleven.dependencies.add(ten);
        eight.dependencies.add(nine);
        eight.dependencies.add(ten);
        List<Resource> list = new ArrayList<>(Arrays.asList(seven, five, three, eleven, eight, two, nine, ten));
        ResourceSorter.sort(list);
        Assert.assertEquals(list.size(), 8);
    }

    @Test(expectedExceptions = InvalidDependencyException.class)
    public void testSortCircular() throws Exception {
        STContext ctx = new STContext();
        ResourceDefinition rf = new ResourceDefinition("test", "file");
        // circular dependency  5 -> [ 7 -> 11 -> 2 -> 7 ]
        Resource seven = new Resource(ctx, rf, null, null, null);
        Resource five = new Resource(ctx, rf, null, null, null);
        Resource three = new Resource(ctx, rf, null, null, null);
        Resource eleven = new Resource(ctx, rf, null, null, null);
        Resource eight = new Resource(ctx, rf, null, null, null);
        Resource two = new Resource(ctx, rf, null, null, null);
        Resource nine = new Resource(ctx, rf, null, null, null);
        Resource ten = new Resource(ctx, rf, null, null, null);
        seven.dependencies.add(eleven);
        seven.dependencies.add(eight);
        five.dependencies.add(eleven);
        three.dependencies.add(eight);
        three.dependencies.add(ten);
        eleven.dependencies.add(two);
        eleven.dependencies.add(nine);
        eleven.dependencies.add(ten);
        eleven.dependencies.add(two);
        eight.dependencies.add(nine);
        eight.dependencies.add(ten);
        two.dependencies.add(seven);
        five.dependencies.add(seven);
        List<Resource> list = new ArrayList<>(Arrays.asList(seven, five, three, eleven, eight, two, nine, ten));
        ResourceSorter.sort(list);
        // Circular dependency: DataFile[7] -> DataFile[11] -> DataFile[2] -> DataFile[7]
        // Circular dependency: DataFile[7] -> DataFile[11] -> DataFile[2] -> DataFile[7]
    }
}
