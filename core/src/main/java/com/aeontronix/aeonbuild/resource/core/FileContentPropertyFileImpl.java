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

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 13/07/13
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */
public class FileContentPropertyFileImpl implements FileContent {
    @Override
    public InputStream getStream() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] getSha1() {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(String path, @NotNull InputStream stream, @NotNull byte[] sha1) throws KMRuntimeException, IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void merge(List<Resource> fragments) throws KMRuntimeException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
