/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.exception.STRuntimeException;

public abstract class NotificationHandler {
    protected boolean reorder;
    protected boolean aggregate;
    protected boolean onlyIfAfter;
    protected String category;

    protected NotificationHandler(boolean reorder, boolean aggregate, boolean onlyIfAfter, String category) {
        this.reorder = reorder;
        this.aggregate = aggregate;
        this.onlyIfAfter = onlyIfAfter;
        this.category = category;
    }

    public boolean isReorder() {
        return reorder;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public boolean isOnlyIfAfter() {
        return onlyIfAfter;
    }

    public String getCategory() {
        return category;
    }

    public abstract void handleNotification(Notification notification) throws STRuntimeException;
}
