/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.exception.ResourceCreationException;

import java.io.File;

/**
 * Created by yannick on 11/08/2014.
 */
public interface KMContext {
    void registerLibraries(File libDir);

    Resource add(Object javaResource) throws ResourceCreationException;
}
