/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.STRuntimeException;

public interface SyncAction {
    boolean verify(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException;
}
