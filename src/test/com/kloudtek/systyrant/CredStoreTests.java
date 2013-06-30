/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.service.credstore.CredStore;
import com.kloudtek.util.UnableToDecryptException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CredStoreTests extends AbstractContextTest {
    public static final String MYID = "myid";
    public static final String TESTPASSWORD = "TESTPASSWORD";

    @Test
    public void testCredStorePw() throws InvalidServiceException, IOException, UnableToDecryptException {
        testObtainPw(false);
    }

    @Test
    public void testCredStoreCryptedPw() throws InvalidServiceException, IOException, UnableToDecryptException {
        testObtainPw(true);
    }

    private void testObtainPw(boolean crypt) throws InvalidServiceException, IOException, UnableToDecryptException {
        CredStore credStore = ctx.getServiceManager().getService(CredStore.class);
        String pw = credStore.obtainPassword(MYID);
        System.out.println("Generated password: " + pw);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (crypt) {
            credStore.setCryptPw(TESTPASSWORD);
        }
        credStore.save(buf);
        credStore.close();
        if (crypt) {
            credStore.setCryptPw(TESTPASSWORD);
        }
        Assert.assertNull(credStore.getPassword(MYID));
        byte[] data = buf.toByteArray();
        if (crypt) {
            assertFalse(new String(data).contains(pw));
            assertTrue(CredStore.isEncrypted(data));
        }
        credStore.load(new ByteArrayInputStream(data));
        Assert.assertEquals(credStore.getPassword(MYID), pw);
    }
}
