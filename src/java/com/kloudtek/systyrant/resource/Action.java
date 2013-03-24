/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.STRuntimeException;

import java.lang.reflect.Method;

public interface Action extends Comparable<Action> {
    /**
     * Executes the action
     * @param context Context
     * @param resource Resource being executed
     * @throws STRuntimeException If an error occurs during execution
     */
    void execute(STContext context, Resource resource) throws STRuntimeException;

    /**
     * Checks if execution is required.
     * @return True if execution is required.
     * @param context
     * @param resource
     */
    boolean checkExecutionRequired(STContext context, Resource resource) throws STRuntimeException;

    int getOrder();

    Type getType();

    public enum Type {
        INIT, PREPARE, EXECUTE, SYNC, POSTCHILDREN_EXECUTE, POSTCHILDREN_SYNC, CLEANUP;
    }
}
