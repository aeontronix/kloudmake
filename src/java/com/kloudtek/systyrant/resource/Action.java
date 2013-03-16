/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.Resource;

/**
 * Used to invoke actionsByStage during lifecycle stage processing.
 */
public abstract class Action implements Comparable<Action> {
    protected int order;

    protected Action() {
    }

    protected Action(int order) {
        this.order = order;
    }

    /**
     * Execute this action
     *
     * @param context
     * @param resource     Resource owning the action.
     * @param stage        Current stage.
     * @param postChildren If this is a post-children action
     */
    public abstract void execute(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(Action o) {
        return o.order - order;
    }
}
