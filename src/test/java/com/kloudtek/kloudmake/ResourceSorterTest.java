/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.exception.InvalidDependencyException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceSorterTest {
    @Test
    public void testSortSuccessful() throws Exception {
        KMContextImpl ctx = new KMContextImpl();
        ResourceImpl seven = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl five = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl three = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl eleven = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl eight = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl two = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl nine = new ResourceImpl(ctx, null, null, null, null);
        ResourceImpl ten = new ResourceImpl(ctx, null, null, null, null);
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
        List<Resource> list = new ArrayList<>();
        for (ResourceImpl resource : Arrays.asList(seven, five, three, eleven, eight, two, nine, ten)) {
            list.add(resource);
        }
        ResourceSorter.sort(list);
        Assert.assertEquals(list.size(), 8);
    }

    @Test(expectedExceptions = InvalidDependencyException.class)
    public void testSortCircular() throws Exception {
        KMContextImpl ctx = new KMContextImpl();
        ResourceDefinition rf = new ResourceDefinition("test", "file");
        // circular dependency  5 -> [ 7 -> 11 -> 2 -> 7 ]
        ResourceImpl seven = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl five = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl three = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl eleven = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl eight = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl two = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl nine = new ResourceImpl(ctx, rf, null, null, null);
        ResourceImpl ten = new ResourceImpl(ctx, rf, null, null, null);
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
        List<Resource> list = new ArrayList<>();
        for (ResourceImpl resource : Arrays.asList(seven, five, three, eleven, eight, two, nine, ten)) {
            list.add(resource);
        }
        ResourceSorter.sort(list);
        // Circular dependency: DataFile[7] -> DataFile[11] -> DataFile[2] -> DataFile[7]
        // Circular dependency: DataFile[7] -> DataFile[11] -> DataFile[2] -> DataFile[7]
    }
}
