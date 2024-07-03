/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 12/02/2013
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public interface Startable {
    void start() throws KMRuntimeException;
}
