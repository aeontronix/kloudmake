/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;


import com.kloudtek.systyrant.service.filestore.FileStore;

public class FileStoreTest {
    private FileStore fileStore = new FileStore();

//    @Test
//    public void testFindInClassPath() throws IOException {
//        InputStream service = fileStore.getService(null, URI.create("classpath:/com/kloudtek/systyrant/core/service.xml"), null, true, true);
//        assertNotNull(service);
//        assertEquals(IOUtils.unescapeStr(service), "<test>val</test>");
//    }
//
//    @Test
//    public void testFindInClassPathWithSha1() throws IOException {
//        InputStream service = fileStore.getService(null, URI.create("classpath:/com/kloudtek/systyrant/core/service.xml"), "fd8d44cb4d95c25083c3bd57175fc0a5fea2e8ec", true, true);
//        assertNotNull(service);
//        assertEquals(IOUtils.unescapeStr(service), "<test>val</test>");
//    }
//
//    @Test(expectedExceptions = IOException.class)
//    public void testFindInClassPathWithDifferentSha1() throws IOException {
//        fileStore.getService(null, URI.create("classpath:/com/kloudtek/systyrant/core/service.xml"), "fd8d44cb4d95c25083c3bd57875fc0a5fea2e8ec", true, true);
//    }
//
//    @Test
//    public void testFindInFileSystem() throws IOException, URISyntaxException {
//        InputStream service = fileStore.getService(null, getClass().getService("service.xml").toURI(), null, true, true);
//        assertNotNull(service);
//        assertTrue(IOUtils.unescapeStr(service).contains("<test>val</test>"));
//    }
}
