/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.STRuntimeException;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 18/03/13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public interface Action extends Comparable<Action> {
    /**
     * Execute this action
     *
     * @param context
     * @param resource     Resource owning the action.
     * @param stage        Current stage.
     * @param postChildren If this is a post-children action
     */
    void execute(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException;

    int getOrder();

    void setOrder(int order);
}
