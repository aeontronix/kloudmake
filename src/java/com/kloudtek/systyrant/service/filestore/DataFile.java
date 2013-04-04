/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.filestore;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Provides access to a file in the {@link FileStore}
 */
public abstract class DataFile {
    public abstract InputStream getStream() throws IOException;

    public abstract byte[] getSha1() throws IOException;

    public String copyToTemp() throws STRuntimeException {
        try {
            InputStream fileStream = new BufferedInputStream(getStream());
            Host host = STContext.get().host();
            String tempFile = host.createTempFile();
            host.writeToFile(tempFile, fileStream);
            return tempFile;
        } catch (IOException | InvalidServiceException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    public boolean copyTo(String path) throws STRuntimeException {
        try {
            Host host = STContext.get().host();
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
            throw new STRuntimeException(e.getMessage(), e);
        }
    }
}
