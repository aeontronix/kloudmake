/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.STRuntimeException;

/**
 * Used to invoke actionsByStage during lifecycle stage processing.
 */
public abstract class AbstractTask implements Task {
    protected int order;
    protected Type type = Type.EXECUTE;
    protected String alternative;

    protected AbstractTask() {
    }

    protected AbstractTask(int order, Type type) {
        this.order = order;
        this.type = type;
    }

    public void type(String type) {
        this.type = Type.valueOf(type.toUpperCase());
    }

    public void order(int order) {
        this.order = order;
    }

    public void alt(String alternative) {
        this.alternative = alternative;
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
    public int compareTo(Task o) {
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
