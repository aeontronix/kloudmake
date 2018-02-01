/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.service.filestore;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.exception.InvalidServiceException;
import com.kloudtek.kloudmake.exception.KMRuntimeException;
import com.kloudtek.kloudmake.host.Host;

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
            Host host = KMContextImpl.get().host();
            String tempFile = host.createTempFile();
            host.writeToFile(tempFile, fileStream);
            return tempFile;
        } catch (IOException | InvalidServiceException e) {
            throw new KMRuntimeException(e.getMessage(), e);
        }
    }

    public boolean copyTo(String path) throws KMRuntimeException {
        try {
            Host host = KMContextImpl.get().host();
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
