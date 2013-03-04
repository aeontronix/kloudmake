/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host.metadata;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 03/03/2013
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMetadataProvider implements HostMetadataProvider {
    protected HashMap<String, String> attrs = new HashMap<>();
}
