/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.host;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 03/03/2013
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractHostProvider implements HostProvider {
    protected HashMap<String, String> attrs = new HashMap<>();

    public synchronized Map<String, String> getAttrs() {
        return Collections.unmodifiableMap(attrs);
    }
}
