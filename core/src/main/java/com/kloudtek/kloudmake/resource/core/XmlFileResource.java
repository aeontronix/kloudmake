/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.annotation.Attr;
import com.kloudtek.kloudmake.annotation.FileFragment;
import com.kloudtek.kloudmake.annotation.KMResource;

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
