/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.XmlUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 13/07/13
 * Time: 18:18
 * To change this template use File | Settings | File Templates.
 */
public class FileContentXmlImpl implements FileContent {
    private InputStream stream;
    private byte[] sha1;
    private Document xml;

    @Override
    public InputStream getStream() {
        return stream;
    }

    @Override
    public byte[] getSha1() {
        return sha1;
    }

    @Override
    public void init(String id, @NotNull InputStream stream, @NotNull byte[] sha1) throws STRuntimeException, IOException {
        this.sha1 = sha1;
        try {
            xml = XmlUtils.parse(stream);
        } catch (SAXException e) {
            throw new STRuntimeException("File " + id + " is not a valid XML file");
        }
    }

    @Override
    public void merge(Collection<Resource> fragments) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            XmlUtils.serialize(xml, buf, true, true);
            byte[] data = buf.toByteArray();
            System.out.println(new String(data));
            sha1 = CryptoUtils.sha1(data);
            stream = new ByteArrayInputStream(data);
        } catch (IOException e) {
            throw new RuntimeException("BUG! Got an IOException from a ByteArrayOutputStream ?!", e);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(stream);
    }
}
