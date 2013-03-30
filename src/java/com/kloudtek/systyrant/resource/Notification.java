/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;

import java.util.ArrayList;
import java.util.List;

public class Notification {
    private List<Resource> origins = new ArrayList<>();
    private Resource target;

    public Notification(List<Resource> origins, Resource target) {
        this.origins.addAll(origins);
        this.target = target;
    }

    public Notification(Resource origin, Resource target) {
        this.origins.add(origin);
        this.target = target;
    }

    public List<Resource> getOrigins() {
        return origins;
    }

    public void setOrigins(List<Resource> origins) {
        this.origins = origins;
    }

    public Resource getTarget() {
        return target;
    }

    public void setTarget(Resource target) {
        this.target = target;
    }
}
