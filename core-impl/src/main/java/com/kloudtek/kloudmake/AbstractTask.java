/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.exception.STRuntimeException;

/**
 * Used to invoke actionsByStage during lifecycle stage processing.
 */
public abstract class AbstractTask implements Task {
    protected int order;
    protected Stage stage = Stage.EXECUTE;
    protected boolean postChildren;
    protected String alternative;

    protected AbstractTask() {
    }

    protected AbstractTask(int order, Stage stage) {
        this.order = order;
        this.stage = stage;
    }

    protected AbstractTask(int order, Stage stage, boolean postChildren) {
        this.order = order;
        this.stage = stage;
        this.postChildren = postChildren;
    }

    public void type(Stage stage) {
        this.stage = stage;
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
    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isPostChildren() {
        return postChildren;
    }

    public void setPostChildren(boolean postChildren) {
        this.postChildren = postChildren;
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
    public boolean supports(KMContextImpl context, Resource resource) throws STRuntimeException {
        return true;
    }

    @Override
    public boolean checkExecutionRequired(KMContextImpl context, Resource resource) throws STRuntimeException {
        return true;
    }
}
