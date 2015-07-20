/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.service.credstore;

import com.kloudtek.kloudmake.KMContextImpl;
import com.kloudtek.kloudmake.Resource;
import com.kloudtek.kloudmake.Stage;
import com.kloudtek.kloudmake.annotation.Function;
import com.kloudtek.kloudmake.annotation.Inject;
import com.kloudtek.kloudmake.annotation.Param;
import com.kloudtek.kloudmake.annotation.Service;
import com.kloudtek.kloudmake.exception.InvalidServiceException;
import com.kloudtek.kloudmake.util.AESHelper;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.UnableToDecryptException;
import com.kloudtek.util.xml.XPathUtils;
import com.kloudtek.util.xml.XmlUtils;
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

import static com.kloudtek.util.xml.XmlUtils.createDocument;
import static com.kloudtek.util.xml.XmlUtils.createElement;

@Service
public class CredStore implements AutoCloseable {
    public static final byte[] MAGIC;
    public static final int BLOCKSIZE = 16;
    @Inject
    private KMContextImpl ctx;
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

    public static CredStore get(KMContextImpl ctx) throws InvalidServiceException {
        return ctx.getServiceManager().getService(CredStore.class);
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

    /**
     * This function is used to retrieve a password from the credential store. If the password doesn't exist, one will
     * be generated based on the provided parameters
     *
     * @param id      Password id.
     * @param size    password size.
     * @param caps    Flag indicating if capital letters are required in the password.
     * @param number  Flag indicating if numbers are required in the password.
     * @param symbols Flag indicating if symbos are required in the password.
     * @return Password
     */
    @Function(value = "password", stage = {Stage.PREPARE, Stage.EXECUTE})
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
