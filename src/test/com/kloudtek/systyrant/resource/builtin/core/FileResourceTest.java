/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.filestore.DataFile;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.util.CryptoUtils;
import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.kloudtek.systyrant.FileInfo.Type.DIRECTORY;
import static com.kloudtek.systyrant.FileInfo.Type.FILE;
import static com.kloudtek.systyrant.resource.builtin.core.FileResource.Ensure.ABSENT;
import static com.kloudtek.systyrant.resource.builtin.core.FileResource.Ensure.SYMLINK;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FileResourceTest {
    private Host adminMock = Mockito.mock(Host.class);
    private FileStore fileStoreMock = Mockito.mock(FileStore.class);
    private STContext context;
    public static final String PATH = "/somewhere";
    public static final String PATH2 = "/somewherelse";
    public static final String DATA = "testdata";
    public static final byte[] DATA_SHA = CryptoUtils.sha1(DATA.getBytes());
    public static final byte[] OTHER_SHA = CryptoUtils.sha1("notthesamedata".getBytes());
    private Resource file;
    private static String defaultPermission = "rwx";

    @BeforeMethod
    public void init() throws STRuntimeException, ResourceCreationException, InvalidResourceDefinitionException, InvalidServiceException, InjectException {
        Mockito.reset(adminMock);
        Mockito.reset(fileStoreMock);
        context = new STContext();
        ServiceManager sm = context.getServiceManager();
        context.setHost(adminMock);
        sm.addOverride("filestore", fileStoreMock);
        file = context.getResourceManager().createResource("file");
        file.set("path", PATH);
    }

    @Test
    public void testCreateMissingDir() throws STRuntimeException {
        file.set("ensure", FileResource.Ensure.DIRECTORY);
        fileDoesntExist();
        fileExists();
        assertTrue(context.execute());
        verify(adminMock, times(1)).mkdir(PATH);
    }

    @Test
    public void testCreateExistingDir() throws STRuntimeException {
        file.set("ensure", FileResource.Ensure.DIRECTORY);
        mockGetFileInfo(DIRECTORY, PATH);
        fileExists();
        assertTrue(context.execute());
        verify(adminMock, times(0)).mkdir(PATH);
    }

    private void mockGetFileInfo(FileInfo.Type type, String path) throws STRuntimeException {
        FileInfo fileInfo = new FileInfo(path, type);
        fileInfo.setPermissions(defaultPermission);
        when(adminMock.getFileInfo(PATH)).thenReturn(fileInfo);
    }

    @Test
    public void testCreateExistingSameFile() throws STRuntimeException {
        file.set("ensure", FileResource.Ensure.FILE);
        file.set("content", DATA);
        fileExists();
        mockGetFileInfo(FILE, PATH);
        when(adminMock.getFileSha1(PATH)).thenReturn(DATA_SHA);

        assertTrue(context.execute());

        checkNoWrite();
    }

    @Test
    public void testDeleteExistingFile() throws STRuntimeException {
        file.set("ensure", ABSENT);
        fileExists();
        mockGetFileInfo(FILE, PATH);
        assertTrue(context.execute());

        verify(adminMock).deleteFile(PATH, false);
    }

    @Test
    public void testDeleteNonExistingFile() throws STRuntimeException {
        file.set("ensure", ABSENT);
        fileDoesntExist();
        assertTrue(context.execute());
        verify(adminMock, never()).deleteFile(anyString(), anyBoolean());
    }

    @Test
    public void testCreateSymlink() throws STRuntimeException {
        file.set("ensure", SYMLINK);
        file.set("target", PATH2);
        fileDoesntExist();
        fileExists();
        assertTrue(context.execute());

        verify(adminMock, times(1)).start();
        verify(adminMock, atLeastOnce()).fileExists(PATH);
        verify(adminMock).createSymlink(PATH, PATH2);
        verify(adminMock, times(1)).stop();
    }

    @Test
    public void testCreateFileFromSource() throws STRuntimeException, IOException {
        file.set("source", PATH);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(DATA.getBytes());
        fileDoesntExist();
        fileExists();
        mockGetFileInfo(FILE, PATH);
        when(fileStoreMock.get(PATH)).thenReturn(new DataFile() {
            @Override
            public InputStream getStream() throws IOException {
                return inputStream;
            }

            @Override
            public byte[] getSha1() throws IOException {
                return DATA_SHA;
            }
        });
        assertTrue(context.execute());
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(adminMock).writeToFile(Mockito.eq(PATH), inputStreamArgumentCaptor.capture());
        assertEquals(DATA, IOUtils.toString(inputStreamArgumentCaptor.getValue()));
    }

    private void fileExists() throws STRuntimeException {
        when(adminMock.fileExists(PATH)).thenReturn(true);
    }

    private void fileDoesntExist() throws STRuntimeException {
        when(adminMock.fileExists(PATH)).thenReturn(false);
    }

    private void checkNoWrite() throws STRuntimeException {
        verify(adminMock, never()).writeToFile(any(String.class), any(String.class));
        verify(adminMock, never()).writeToFile(any(String.class), any(byte[].class));
        verify(adminMock, never()).writeToFile(any(String.class), any(InputStream.class));
    }
}
