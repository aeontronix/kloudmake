/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.util;

import com.kloudtek.util.UnableToDecryptException;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 06/04/13
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */
public class AESHelper {
    public static byte[] encrypt(final byte[] data, final String password) {
        final byte[] salt = generateSalt();
        final byte[] cryptedData;
        try {
            cryptedData = crypt(true, data, password, salt);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
        final byte[] merged = new byte[cryptedData.length + 8];
        System.arraycopy(salt, 0, merged, 0, 8);
        System.arraycopy(cryptedData, 0, merged, 8, cryptedData.length);
        return merged;
    }

    public static byte[] decrypt(final byte[] data, final String password) throws UnableToDecryptException {
        final byte[] salt = new byte[8];
        final byte[] cryptedData = new byte[data.length - 8];
        System.arraycopy(data, 0, salt, 0, 8);
        System.arraycopy(data, 8, cryptedData, 0, cryptedData.length);
        try {
            return crypt(false, cryptedData, password, salt);
        } catch (InvalidCipherTextException e) {
            throw new UnableToDecryptException();
        }
    }

    private static byte[] crypt(final boolean encrypt, final byte[] bytes, final String password, final byte[] salt) throws InvalidCipherTextException {
        final PBEParametersGenerator keyGenerator = new PKCS12ParametersGenerator(new SHA256Digest());
        keyGenerator.init(PKCS12ParametersGenerator.PKCS12PasswordToBytes(password.toCharArray()), salt, 20);
        final CipherParameters keyParams = keyGenerator.generateDerivedParameters(256, 128);

        final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        cipher.init(encrypt, keyParams);

        final byte[] processed = new byte[cipher.getOutputSize(bytes.length)];
        int outputLength = cipher.processBytes(bytes, 0, bytes.length, processed, 0);
        outputLength += cipher.doFinal(processed, outputLength);

        final byte[] results = new byte[outputLength];
        System.arraycopy(processed, 0, results, 0, outputLength);
        return results;
    }

    private static byte[] generateSalt() {
        try {
            final byte[] salt = new byte[8];
            final SecureRandom saltGen = SecureRandom.getInstance("SHA1PRNG");
            saltGen.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
