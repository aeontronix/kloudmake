/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

public class Notification {
    private String category;
    private Resource source;
    private Resource target;

    public Notification(String category, Resource source, Resource target) {
        this.category = category;
        this.source = source;
        this.target = target;
    }

    public Notification(Resource source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Resource getSource() {
        return source;
    }

    public void setSource(Resource source) {
        this.source = source;
    }

    public Resource getTarget() {
        return target;
    }

    public void setTarget(Resource target) {
        this.target = target;
    }
}
