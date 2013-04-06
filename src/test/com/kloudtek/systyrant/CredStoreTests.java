/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.service.credstore.CredStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CredStoreTests extends AbstractContextTest {
    private CredStore credStore;

    @BeforeMethod
    public void init() throws InvalidServiceException {
//        credStore = ctx.getServiceManager().getService(CredStore.class);
    }

    @Test
    public void testCredStore() {
        for (int i = 91; i < 97; i++) {
            System.out.print((char) i);
        }
        System.out.println();
        System.out.println((int)'a'); // 97
        System.out.println((int)'z'); // 122
        System.out.println((int)'A'); // 65
        System.out.println((int)'Z'); // 90
        System.out.println((int)'0'); // 48
        System.out.println((int)'9'); // 57
    }
}
