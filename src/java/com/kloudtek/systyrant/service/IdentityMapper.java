/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service;

import com.kloudtek.systyrant.exception.STRuntimeException;

/**
 * Use to map a user identity from one system to another.
 * So for example, a user could have an account on system A that has a username on that system of 'johns', while on the
 * other system he might be 'jsmith', by calling mapIdentity it's possible to find the corresponding id on the other
 * system.
 */
public interface IdentityMapper {
    public static final String DEFAULTID = "identitymapper";

    /**
     * Map an identity of an user in system 'origin'
     *
     * @param userid      User id in origin system
     * @param origin      Origin system id
     * @param destination Destination system id
     * @return User id in 'destination' system, or null if user was not found in either systems.
     */
    String mapIdentity(String userid, String origin, String destination) throws STRuntimeException;
}
