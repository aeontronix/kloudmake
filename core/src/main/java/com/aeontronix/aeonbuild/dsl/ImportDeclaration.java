/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.dsl;

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
