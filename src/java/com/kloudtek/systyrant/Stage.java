/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.resource.Resource;

import static com.kloudtek.systyrant.resource.Resource.State.*;

/**
 * enum of all context execution lifecycle stages
 */
public enum Stage {
    PREPARE(PREPARED), EXECUTE(EXECUTED), CLEANUP(CLEANEDUP);
    private final Resource.State state;

    private Stage(Resource.State state) {
        this.state = state;
    }

    public Resource.State getState() {
        return state;
    }
}
