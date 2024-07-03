/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.FQName;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 13/07/13
 * Time: 18:21
 * To change this template use File | Settings | File Templates.
 */
public class FileFragmentDef {
    private Class<? extends FileContent> fileContentClass;
    private FQName resourceType;

    public FileFragmentDef(Class<? extends FileContent> fileContentClass, FQName fqName) {
        this.fileContentClass = fileContentClass;
        resourceType = fqName;
    }

    public Class<? extends FileContent> getFileContentClass() {
        return fileContentClass;
    }

    public FQName getResourceType() {
        return resourceType;
    }
}
