/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import org.jetbrains.annotations.NotNull;

import static com.kloudtek.util.StringUtils.isEmpty;

public abstract class NotificationHandler {
    protected boolean reorder;
    protected boolean aggregate;
    protected boolean onlyIfAfter;
    protected String category;

    protected NotificationHandler(boolean reorder, boolean aggregate, boolean onlyIfAfter, String category) {
        this.reorder = reorder;
        this.aggregate = aggregate;
        this.onlyIfAfter = onlyIfAfter;
        this.category = category == null ? "" : category;
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

    @NotNull
    public String getCategory() {
        return category;
    }

    public boolean isSameCategory(String handlerCategory) {
        return handlerCategory.isEmpty() ? isEmpty(category) : handlerCategory.equalsIgnoreCase(category);
    }

    public abstract void handleNotification(Notification notification) throws KMRuntimeException;
}
