/*
 * Copyright (c) 2014 KloudTek Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.STRuntimeException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by yannick on 10/08/2014.
 */
public interface FileContent {
    InputStream getStream();

    byte[] getSha1();

    void init(String path, @NotNull InputStream stream, @NotNull byte[] sha1) throws STRuntimeException, IOException;

    void merge(Collection<Resource> fragments) throws STRuntimeException;

    void close();
}
