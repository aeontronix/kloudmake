/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.STRuntimeException;

/**
 * Used to invoke actionsByStage during lifecycle stage processing.
 */
public abstract class AbstractAction implements Action {
    protected int order;
    protected Type type = Type.EXECUTE;
    protected String alternative;

    protected AbstractAction() {
    }

    protected AbstractAction(int order, Type type) {
        this.order = order;
        this.type = type;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int compareTo(Action o) {
        return o.getOrder() - order;
    }

    @Override
    public String getAlternative() {
        return alternative;
    }

    @Override
    public boolean supports(STContext context, Resource resource) throws STRuntimeException {
        return true;
    }

    @Override
    public boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException {
        return true;
    }
}
