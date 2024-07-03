/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

/**
 * Resource uniqueness scopes.
 * <p>GLOBAL indicates that a resource must be unique globally.</p>
 * <p>HOST indicates that a resource must be unique within the scope of a host.</p>
 */
public enum UniqueScope {
    GLOBAL, HOST
}
