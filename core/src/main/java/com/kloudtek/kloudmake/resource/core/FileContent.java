/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.KMRuntimeException;

import javax.validation.constraints.NotNull;
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
