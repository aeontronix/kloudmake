/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ymenager
 * Date: 31/03/2013
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
public class AutoNotify {
    private Set<Resource> sources = new HashSet<>();
    private Resource target;
    @NotNull
    private String category;
    private Set<Resource> sourcesToExecuted = new HashSet<>();

    public AutoNotify(ArrayList<Resource> sources, Resource target, String category) {
        this.category = category == null ? "" : category;
        this.sources.addAll(sources);
        this.target = target;
    }

    public AutoNotify(Resource source, Resource target, String category) {
        this.category = category == null ? "" : category;
        this.sources.add(source);
        this.target = target;
    }

    public void prepare() {
        sourcesToExecuted.clear();
        sourcesToExecuted.addAll(sources);
    }

    public boolean execute(Resource resource) {
        sourcesToExecuted.remove(resource);
        return target.getState().ordinal() >= Stage.EXECUTE.ordinal() && sourcesToExecuted.isEmpty();
    }

    public void merge(AutoNotify autoNotify) {
        throw new RuntimeException("TODO");
    }

    public Set<Resource> getSources() {
        return sources;
    }

    public void setSources(Set<Resource> sources) {
        this.sources = sources;
    }

    public Resource getTarget() {
        return target;
    }

    public void setTarget(Resource target) {
        this.target = target;
    }

    @NotNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NotNull String category) {
        this.category = category;
    }
}
