/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.STRuntimeException;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 12/02/2013
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public interface Startable {
    void start() throws STRuntimeException;
}
