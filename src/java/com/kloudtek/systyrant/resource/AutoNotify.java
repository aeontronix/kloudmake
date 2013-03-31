/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 31/03/2013
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
public class AutoNotify {
    private ArrayList<Resource> sources = new ArrayList<>();
    private Resource target;
    private String category;
    private boolean notificationRequireOrder;
    private boolean notificationAggregate;
    private boolean notificationOnlyAfter;

    public AutoNotify(ArrayList<Resource> sources, Resource target, String category) {
        this.category = category;
        this.sources.addAll(sources);
        this.target = target;
    }

    public AutoNotify(Resource source, Resource target, String category) {
        this.category = category;
        this.sources.add(source);
        this.target = target;
    }

    public void merge(AutoNotify autoNotify) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public ArrayList<Resource> getSources() {
        return sources;
    }

    public void setSources(ArrayList<Resource> sources) {
        this.sources = sources;
    }

    public Resource getTarget() {
        return target;
    }

    public void setTarget(Resource target) {
        this.target = target;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isNotificationRequireOrder() {
        return notificationRequireOrder;
    }

    public void setNotificationRequireOrder(boolean notificationRequireOrder) {
        this.notificationRequireOrder = notificationRequireOrder;
    }

    public boolean isNotificationAggregate() {
        return notificationAggregate;
    }

    public void setNotificationAggregate(boolean notificationAggregate) {
        this.notificationAggregate = notificationAggregate;
    }

    public boolean isNotificationOnlyAfter() {
        return notificationOnlyAfter;
    }

    public void setNotificationOnlyAfter(boolean notificationOnlyAfter) {
        this.notificationOnlyAfter = notificationOnlyAfter;
    }
}
