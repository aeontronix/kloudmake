/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.java;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.host.OperatingSystem;
import com.kloudtek.systyrant.resource.Resource;

/**
* Created with IntelliJ IDEA.
* User: yannick
* Date: 24/03/13
* Time: 17:12
* To change this template use File | Settings | File Templates.
*/
public class EnforceOnlyIfOS extends EnforceOnlyIf {
    private OperatingSystem os;

    public EnforceOnlyIfOS(OperatingSystem os) {
        this.os = os;
    }

    @Override
    public boolean execAllowed(STContext context, Resource resource) {
        Host host = resource.getHost();
        return host.getMetadata().getOperatingSystem().equals(os);
    }
}
