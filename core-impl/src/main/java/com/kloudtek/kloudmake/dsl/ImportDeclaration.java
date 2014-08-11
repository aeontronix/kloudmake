/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.dsl;

public class ImportDeclaration {
    private String path;

    public ImportDeclaration(String path) {
        this.path = path;
    }

    public ImportDeclaration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
