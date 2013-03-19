/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

/**
 * Used to invoke actionsByStage during lifecycle stage processing.
 */
public abstract class AbstractAction implements Action {
    protected int order;

    protected AbstractAction() {
    }

    protected AbstractAction(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(Action o) {
        return o.getOrder() - order;
    }
}
