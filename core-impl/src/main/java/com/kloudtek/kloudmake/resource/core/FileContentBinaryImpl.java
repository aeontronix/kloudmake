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
public class FileContentBinaryImpl implements FileContent {
    @Override
    public InputStream getStream() {
        return null;
    }

    @Override
    public byte[] getSha1() {
        return new byte[0];
    }

    @Override
    public void init(String path, @NotNull InputStream stream, @NotNull byte[] sha1) throws STRuntimeException, IOException {

    }

    @Override
    public void merge(Collection<Resource> fragments) throws STRuntimeException {

    }

    @Override
    public void close() {

    }
}
