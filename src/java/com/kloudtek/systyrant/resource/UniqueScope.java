/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

/**
 * Resource uniqueness scopes.
 * <p>GLOBAL indicates that a resource must be unique globally.</p>
 * <p>HOST indicates that a resource must be unique within the scope of a host.</p>
 */
public enum UniqueScope {
    GLOBAL, HOST
}
