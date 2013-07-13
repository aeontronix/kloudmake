/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.FileFragment;
import com.kloudtek.systyrant.annotation.STResource;

@STResource
@FileFragment(fileContentClass = FileContentPropertyFileImpl.class)
public class PropertyFileResource {
    @Attr(required = true)
    private String path;
    @Attr(required = true)
    private String xpath;
    @Attr(required = true)
    private String key;
    @Attr(required = true)
    private String value;
}
