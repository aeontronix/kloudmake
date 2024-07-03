/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.service.filestore;

import com.aeontronix.aeonbuild.exception.InvalidServiceException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.host.Host;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Provides access to a file in the {@link FileStore}
 */
public abstract class DataFile implements AutoCloseable {
    public abstract InputStream getStream() throws IOException;

    public abstract byte[] getSha1() throws IOException;

    public String copyToTemp() throws KMRuntimeException {
        try {
            InputStream fileStream = new BufferedInputStream(getStream());
            Host host = BuildContextImpl.get().host();
            String tempFile = host.createTempFile();
            host.writeToFile(tempFile, fileStream);
            return tempFile;
        } catch (IOException | InvalidServiceException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    public boolean copyTo(String path) throws KMRuntimeException {
        try {
            Host host = BuildContextImpl.get().host();
            if (host.fileExists(path)) {
                byte[] hostSha1 = host.getFileSha1(path);
                if (hostSha1 != null) {
                    if (Arrays.equals(hostSha1, getSha1())) {
                        return false;
                    }
                }
            }
            host.writeToFile(path, getStream());
            return true;
        } catch (IOException | InvalidServiceException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }
}
