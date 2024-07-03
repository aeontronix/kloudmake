/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.ResourceCreationException;

import java.io.File;

/**
 * Created by yannick on 11/08/2014.
 */
public interface KMContext {
    void registerLibraries(File libDir);

    Resource add(Object javaResource) throws ResourceCreationException;
}
