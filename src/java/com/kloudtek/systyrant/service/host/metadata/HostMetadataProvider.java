/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host.metadata;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.host.Host;

import java.util.HashMap;

public interface HostMetadataProvider {
    boolean supports(Host host, HashMap<String, Object> datacache) throws STRuntimeException;

    OperatingSystem getOperatingSystem();
}
