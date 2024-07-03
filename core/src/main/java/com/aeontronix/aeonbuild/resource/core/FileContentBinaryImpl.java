/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.Resource;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by yannick on 10/08/2014.
 */
public class FileContentBinaryImpl implements FileContent {
    private byte[] data;
    private byte[] sha1;

    @Override
    public InputStream getStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public byte[] getSha1() {
        return sha1;
    }

    @Override
    public void init(String path, @NotNull InputStream stream, @NotNull byte[] sha1) throws KMRuntimeException, IOException {
        this.sha1 = sha1;
        data = IOUtils.toByteArray(stream);
    }

    @Override
    public void merge(List<Resource> fragments) throws KMRuntimeException {
        throw new KMRuntimeException("Binary file merge not supported");
    }

    @Override
    public void close() {
    }
}
