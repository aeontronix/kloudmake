/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.FileFragment;
import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.annotation.Attr;

@KMResource
@FileFragment(fileContentClass = FileContentXmlImpl.class)
public class XmlFileResource {
    @Attr(required = true)
    private String path;
    @Attr(required = true, def = "insert")
    private Type type = Type.INSERT;
    @Attr(required = true)
    private String xpath;
    @Attr
    private String xml;

    public enum Type {
        INSERT, REPLACE, DELETE
    }
}
