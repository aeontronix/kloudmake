/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.annotation.Attr;
import com.kloudtek.kloudmake.annotation.FileFragment;
import com.kloudtek.kloudmake.annotation.KMResource;

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
