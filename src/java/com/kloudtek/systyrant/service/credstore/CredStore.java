/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.credstore;

import com.kloudtek.systyrant.annotation.Service;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;

@Service
public class CredStore {
    private final SecureRandom rng = new SecureRandom();
    private static final char[] supportedSymbols = new char[]{'!', '@', '$', '%', '^', '&', '*', '(', ')', '-', '=',
            '[', ']', '{', '}', ';', ':', '|', '<', '>', '?'};
    private HashMap<String,String> passwords = new HashMap<>();
    private String cryptPw;

    public String generatePassword(int size, boolean caps, boolean number, boolean symbols) {
        if( size <= 4 ) {
            throw new IllegalArgumentException("Password size must be at least 4");
        }
        int nb = (size / 10) + 1;
        char[] pw = new char[size];
        for (int i = 0; i <= nb; i++) {
            int idx = getRandomPwSlot(pw);
            if( caps ) {
                pw[idx] = (char)(rng.nextInt(26) + 65);
            }
            idx = getRandomPwSlot(pw);
            if( number ) {
                pw[idx] = (char)rng.nextInt(10);
            }
            idx = getRandomPwSlot(pw);
            if( symbols ) {
                pw[idx] = supportedSymbols[rng.nextInt(supportedSymbols.length)];
            }
        }
        for (int i = 0; i < size; i++) {
            if( pw[i] != 0 ) {
                pw[i] = (char)(rng.nextInt(26)+97);
            }
        }
        return new String(pw);
    }

    private int getRandomPwSlot(char[] pw) {
        int i = rng.nextInt(pw.length - 1);
        while ( pw[i] != 0 ) {
            i++;
            if( i > pw.length ) {
                i = 0;
            }
        }
        return i;
    }

    public synchronized String getPassword(String id, int size, boolean caps, boolean number, boolean symbols) {
        String pw = passwords.get(id);
        if( pw == null ) {
            pw = generatePassword(size, caps, number, symbols);
            passwords.put(id, pw);
        }
        return pw;
    }

    public void setCryptPw(String cryptPw) {
        this.cryptPw = cryptPw;
    }

    public void load( InputStream is ) {
        if( cryptPw != null ) {

        }
    }

    private enum Type {
        LCA, UCA, NB, SYM
    }
}
