/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.vagrant;

import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.STResource;

@STResource
public class SharedFolder {
    @Attr(def = "true")
    private boolean create;
    @Attr("transient")
    private boolean transientFolder;
    @Attr
    private String name;
    @Attr
    private String local;
    @Attr
    private String remote;

    public SharedFolder() {
    }

    public SharedFolder(boolean create, boolean transientFolder, String name, String local, String remote) {
        this.create = create;
        this.transientFolder = transientFolder;
        this.name = name;
        this.local = local;
        this.remote = remote;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isTransientFolder() {
        return transientFolder;
    }

    public void setTransientFolder(boolean transientFolder) {
        this.transientFolder = transientFolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
