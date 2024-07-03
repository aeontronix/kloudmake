/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.FileFragment;
import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.annotation.Attr;

@KMResource
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
