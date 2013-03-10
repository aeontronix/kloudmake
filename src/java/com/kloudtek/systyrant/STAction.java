/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;

/** Used to invoke actionsByStage during lifecycle stage processing. */
public interface STAction {
    /**
     * Execute this action
     *
     * @param resource     Resource owning the action.
     * @param stage        Current stage.
     * @param postChildren
     */
    public abstract void execute(Resource resource, Stage stage, boolean postChildren) throws STRuntimeException;
}
