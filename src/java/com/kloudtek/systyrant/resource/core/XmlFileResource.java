/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.STResource;

@STResource
public class XmlFileResource {
    @Attr(required = true)
    private String path;
    @Attr(required = true, def = "insert")
    private Type type = Type.INSERT;
    @Attr
    private String xml;

    public enum Type {
        INSERT, REPLACE, DELETE
    }
}
