/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.exception.STRuntimeException;
import com.kloudtek.util.crypto.CryptoUtils;
import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.XPathUtils;
import com.kloudtek.util.XmlUtils;
import com.kloudtek.util.crypto.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.validation.constraints.NotNull;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
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
    public void merge(Collection<Resource> fragments) throws STRuntimeException {
        for (Resource fragment : fragments) {
            String uid = fragment.getUid();
            String type = fragment.get("type").toLowerCase();
            String val = fragment.get("xml");
            String xpath = fragment.get("xpath");
            try {
                Node node = XPathUtils.evalXPathNode(xpath, xml);
                Node xmlFragment = null;
                if (val != null) {
                    xmlFragment = xml.importNode(XmlUtils.parse(new StringReader(val)).getDocumentElement(), true);
                } else {
                    if (!type.equals("delete")) {
                        throw new STRuntimeException("Missing xml attribute in file fragment " + uid + " : " + type);
                    }
                }
                switch (type) {
                    case "insert":
                        node.appendChild(xmlFragment);
                        break;
                    case "replace":
                        node.getParentNode().replaceChild(xmlFragment, node);
                        break;
                    case "delete":
                        node.getParentNode().removeChild(node);
                        break;
                    default:
                        throw new STRuntimeException("Invalid type in file fragment " + uid + " : " + type);
                }
            } catch (XPathExpressionException e) {
                throw new STRuntimeException("Invalid xpath value in file fragment " + uid + " : " + xpath);
            } catch (SAXException e) {
                throw new STRuntimeException("Invalid xml value in file fragment " + uid + " : " + xml);
            } catch (IOException e) {
                throw new UnexpectedException(e);
            }
        }
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            XmlUtils.serialize(xml, buf, true, true);
            byte[] data = buf.toByteArray();
            sha1 = DigestUtils.sha1(data);
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
