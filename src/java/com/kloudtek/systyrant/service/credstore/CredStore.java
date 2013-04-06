/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.credstore;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.annotation.Inject;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.Param;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.util.AESHelper;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.UnableToDecryptException;
import com.kloudtek.util.XPathUtils;
import com.kloudtek.util.XmlUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kloudtek.util.XmlUtils.createDocument;
import static com.kloudtek.util.XmlUtils.createElement;

@Service
public class CredStore implements AutoCloseable {
    public static final byte[] MAGIC;
    public static final int BLOCKSIZE = 16;
    @Inject
    private STContext ctx;
    private final SecureRandom rng = new SecureRandom();
    private static final char[] supportedSymbols = new char[]{'!', '@', '$', '%', '^', '&', '*', '(', ')', '-', '=',
            '[', ']', '{', '}', ';', ':', '|', '<', '>', '?'};
    private HashMap<String, String> passwords = new HashMap<>();
    private String cryptPw;

    static {
        try {
            MAGIC = "STC".getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("BUG: " + e.getMessage(), e);
        }
    }

    public static CredStore get(STContext ctx) throws InvalidServiceException {
        return ctx.getServiceManager().getService(CredStore.class);
    }

    public CredStore() {
        System.out.println();
    }

    public void close() {
        passwords.clear();
        cryptPw = null;
    }

    private String generatePassword(int size, boolean caps, boolean number, boolean symbols) {
        if (size <= 4) {
            throw new IllegalArgumentException("Password size must be at least 4");
        }
        int nb = (size / 10) + 1;
        char[] pw = new char[size];
        for (int i = 0; i <= nb; i++) {
            int idx = getRandomPwSlot(pw);
            if (caps) {
                pw[idx] = (char) (rng.nextInt(26) + 65);
            }
            idx = getRandomPwSlot(pw);
            if (number) {
                pw[idx] = (char) (rng.nextInt(10) + 48);
            }
            idx = getRandomPwSlot(pw);
            if (symbols) {
                pw[idx] = supportedSymbols[rng.nextInt(supportedSymbols.length)];
            }
        }
        for (int i = 0; i < size; i++) {
            if (pw[i] == 0) {
                pw[i] = (char) (rng.nextInt(26) + 97);
            }
        }
        return new String(pw);
    }

    private int getRandomPwSlot(char[] pw) {
        int i = rng.nextInt(pw.length - 1);
        while (pw[i] != 0) {
            i++;
            if (i >= pw.length) {
                i = 0;
            }
        }
        return i;
    }

    public synchronized String getPassword(String id) {
        return passwords.get(id);
    }

    public synchronized String obtainPassword(String id) {
        return obtainPassword(id, 20, true, true, true);
    }

    public synchronized String obtainPassword(String id, int size) {
        return obtainPassword(id, size, true, true, true);
    }

    @Method(value = "password", stage = {Stage.PREPARE, Stage.EXECUTE})
    public synchronized String obtainPassword(@Param("id") String id, @Param(value = "size", def = "20") int size,
                                              @Param(value = "caps", def = "true") boolean caps,
                                              @Param(value = "number", def = "true") boolean number,
                                              @Param(value = "symbols", def = "true") boolean symbols) {
        if (id == null) {
            Resource resource = ctx.currentResource();
            if (resource == null) {
                throw new IllegalArgumentException("id is missing and no resource is in context");
            }
            id = resource.getUid();
        }
        String pw = passwords.get(id);
        if (pw == null) {
            pw = generatePassword(size, caps, number, symbols);
            passwords.put(id, pw);
        }
        return pw;
    }

    public void setCryptPw(String cryptPw) {
        this.cryptPw = cryptPw;
    }

    public void load(InputStream is) throws IOException, UnableToDecryptException {
        byte[] xml = IOUtils.toByteArray(is);
        if (xml.length < 4) {
            throw new IOException("XML credentials file is invalid");
        }
        boolean cryptedFile = isEncrypted(xml);
        if (cryptedFile) {
            if (cryptPw == null) {
                throw new UnableToDecryptException("Credential file is encrypted and no password is set");
            }
            xml = AESHelper.decrypt(Arrays.copyOfRange(xml, MAGIC.length, xml.length), cryptPw);
        }
        try {
            ByteArrayInputStream buf = new ByteArrayInputStream(xml);
            Document doc = XmlUtils.parse(buf);
            IOUtils.closeQuietly(buf);
            for (Element element : XPathUtils.evalXPathElements("credentials/password", doc)) {
                String id = element.getAttribute("id");
                if (StringUtils.isEmpty(id)) {
                    throw new IOException("XML credentials file is invalid, password missing id");
                }
                String password = element.getTextContent().trim();
                if (StringUtils.isEmpty(password)) {
                    throw new IOException("XML credentials file is invalid, password is missing");
                }
                passwords.put(id, password);
            }
        } catch (SAXException e) {
            throw new IOException("XML credentials file is invalid");
        } catch (XPathExpressionException e) {
            throw new IOException("BUG: Invalid Xpath expression: " + e.getMessage());
        }
    }

    public void save(OutputStream out) throws IOException {
        Document doc = createDocument();
        Element root = createElement("credentials", doc);
        for (Map.Entry<String, String> entry : passwords.entrySet()) {
            Element pw = createElement("password", root, "id", entry.getKey());
            pw.setTextContent(entry.getValue());
        }
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        XmlUtils.serialize(doc, xml, true, true);
        IOUtils.closeQuietly(xml);
        final byte[] xmlBytes;
        if (cryptPw != null) {
            xmlBytes = AESHelper.encrypt(xml.toByteArray(), cryptPw);
        } else {
            xmlBytes = xml.toByteArray();
        }
        if (cryptPw != null) {
            out.write(MAGIC);
        }
        IOUtils.write(xmlBytes, out);
    }

    public static boolean isEncrypted(byte[] xml) {
        return Arrays.equals(MAGIC, Arrays.copyOf(xml, MAGIC.length));
    }
}
