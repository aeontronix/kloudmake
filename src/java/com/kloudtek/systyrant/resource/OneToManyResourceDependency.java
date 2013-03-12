/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidDependencyException;
import com.kloudtek.systyrant.exception.InvalidQueryException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is used to allow the creation to other elements as a string description, which is resolved right before the
 * execution stage (so the target elements could not even exist when the ref is created).
 */
public class OneToManyResourceDependency implements ResourceDependency {
    private Resource origin;
    private Set<Resource> targets = new HashSet<>();
    private String targetRef;
    private boolean optional;

    public OneToManyResourceDependency() {
    }

    public OneToManyResourceDependency(Resource origin, Collection<Resource> targets) {
        this.origin = origin;
        this.targets.addAll(targets);
    }

    public OneToManyResourceDependency(Resource origin, String ref, boolean optional) {
        this.origin = origin;
        this.targetRef = ref;
        this.optional = optional;
    }

    public OneToManyResourceDependency(Resource origin, Resource target, boolean optional) {
        this.origin = origin;
        this.targets.add(target);
        this.optional = optional;
    }

    public void resolve(STContext context) throws InvalidDependencyException {
        try {
            if(targetRef != null) {
                List<Resource> resources = context.findResources(targetRef);
                resources.remove(origin);
                targets.addAll(resources);
            }
        } catch (InvalidQueryException e) {
            throw new InvalidDependencyException(e.getMessage(), e);
        }
        if( ( targets.isEmpty() ) && ! optional ) {
            throw new InvalidDependencyException("Mandatory dependency has not valid targets: "+toString());
        }
    }

    @Override
    public String toString() {
        return "OneToManyResourceDependency{" +
                "origin=" + origin +
                ", targets=" + targets +
                ", targetRef='" + targetRef + '\'' +
                ", optional=" + optional +
                '}';
    }

    public Resource getOrigin() {
        return origin;
    }

    public void setOrigin(Resource origin) {
        this.origin = origin;
    }

    public Set<Resource> getTargets() {
        return targets;
    }

    public void setTargets(Set<Resource> targets) {
        this.targets = targets;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}
