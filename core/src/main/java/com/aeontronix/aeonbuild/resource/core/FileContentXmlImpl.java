/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.Resource;
import com.kloudtek.kryptotek.DigestUtils;
import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.IOUtils;
import com.kloudtek.util.xml.XPathUtils;
import com.kloudtek.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.jetbrains.annotations.NotNull;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.List;

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
    public void init(String id, @NotNull InputStream stream, @NotNull byte[] sha1) throws KMRuntimeException, IOException {
        this.sha1 = sha1;
        try {
            xml = XmlUtils.parse(stream);
        } catch (SAXException e) {
            throw new KMRuntimeException("File " + id + " is not a valid XML file");
        }
    }

    @Override
    public void merge(List<Resource> fragments) throws KMRuntimeException {
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
                        throw new KMRuntimeException("Missing xml attribute in file fragment " + uid + " : " + type);
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
                        throw new KMRuntimeException("Invalid type in file fragment " + uid + " : " + type);
                }
            } catch (XPathExpressionException e) {
                throw new KMRuntimeException("Invalid xpath value in file fragment " + uid + " : " + xpath);
            } catch (SAXException e) {
                throw new KMRuntimeException("Invalid xml value in file fragment " + uid + " : " + xml);
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
        IOUtils.close(stream);
    }
}
