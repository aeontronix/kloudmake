/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

public abstract class NotificationHandler {
    private boolean reorder;
    private boolean aggregate;
    private boolean onlyIfAfter;

    protected NotificationHandler() {
    }

    protected NotificationHandler(boolean reorder, boolean aggregate, boolean onlyIfAfter) {
        this.reorder = reorder;
        this.aggregate = aggregate;
        this.onlyIfAfter = onlyIfAfter;
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

    public abstract void handleNotification( Notification notification );
}
