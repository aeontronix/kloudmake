/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.resource;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.STContext;
import com.kloudtek.kloudmake.ServiceManager;
import com.kloudtek.kloudmake.exception.InjectException;
import com.kloudtek.kloudmake.exception.InvalidResourceDefinitionException;
import com.kloudtek.kloudmake.exception.InvalidServiceException;
import com.kloudtek.kloudmake.exception.STRuntimeException;
import com.kloudtek.kloudmake.host.FileInfo;
import com.kloudtek.kloudmake.host.Host;
import com.kloudtek.kloudmake.resource.core.FileResource;
import com.kloudtek.kloudmake.service.filestore.DataFile;
import com.kloudtek.kloudmake.service.filestore.FileStore;
import com.kloudtek.util.crypto.CryptoUtils;
import com.kloudtek.util.crypto.DigestUtils;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.kloudtek.kloudmake.host.FileInfo.Type.DIRECTORY;
import static com.kloudtek.kloudmake.host.FileInfo.Type.FILE;
import static com.kloudtek.kloudmake.resource.core.FileResource.Ensure.ABSENT;
import static com.kloudtek.kloudmake.resource.core.FileResource.Ensure.SYMLINK;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FileResourceMockedTest {
    private Host adminMock = mock(Host.class);
    private FileStore fileStoreMock = mock(FileStore.class);
    private STContext context;
    public static final String PATH = "/somewhere";
    public static final String PATH2 = "/somewherelse";
    public static final String DATA = "testdata";
    public static final byte[] DATA_SHA = DigestUtils.sha1(DATA.getBytes());
    public static final byte[] OTHER_SHA = DigestUtils.sha1("notthesamedata".getBytes());
    private Resource file;
    private static String defaultPermission = "rwx";

    @BeforeMethod
    public void init() throws STRuntimeException, InvalidResourceDefinitionException, InvalidServiceException, InjectException {
        reset(adminMock);
        reset(fileStoreMock);
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
        execute();

        verify(adminMock, atLeastOnce()).fileExists(PATH);
        verify(adminMock, times(1)).createSymlink(PATH, PATH2);
    }

    private void execute() throws STRuntimeException {
        assertTrue(context.execute());
    }

    @Test
    public void testCreateFileFromSource() throws STRuntimeException, IOException, TemplateException {
        context.setFatalExceptions(Exception.class);
        file.set("source", PATH);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(DATA.getBytes());
        fileDoesntExist();
        fileExists();
        mockGetFileInfo(FILE, PATH);
        when(fileStoreMock.create(PATH)).thenReturn(new DataFile() {
            @Override
            public InputStream getStream() throws IOException {
                return inputStream;
            }

            @Override
            public void close() throws Exception {
            }

            @Override
            public byte[] getSha1() throws IOException {
                return DATA_SHA;
            }
        });
        assertTrue(context.execute());
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(adminMock).writeToFile(eq(PATH), inputStreamArgumentCaptor.capture());
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