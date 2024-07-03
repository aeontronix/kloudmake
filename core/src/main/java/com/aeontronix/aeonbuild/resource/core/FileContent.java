/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileContent {
    InputStream getStream();

    byte[] getSha1();

    void init(String path, @NotNull InputStream stream, @NotNull byte[] sha1) throws KMRuntimeException, IOException;

    void merge(List<Resource> fragments) throws KMRuntimeException;

    void close();
}
