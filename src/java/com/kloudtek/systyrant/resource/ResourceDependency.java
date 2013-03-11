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
public class ResourceDependency {
    private Set<Resource> origins = new HashSet<>();
    private Set<Resource> targets = new HashSet<>();
    private String originRef;
    private String targetRef;
    private boolean optional;

    public ResourceDependency() {
    }

    public ResourceDependency(Resource origin, Resource target) {
        origins.add(origin);
        targets.add(target);
    }

    public ResourceDependency(Resource origin, Resource target, boolean optional) {
        this(origin, target);
        this.optional = optional;
    }

    public ResourceDependency(Resource origin, String target, boolean optional) {
        origins.add(origin);
        targetRef = target;
        this.optional = optional;
    }

    public ResourceDependency(Resource origin, Collection<Resource> targets) {
        origins.add(origin);
        this.targets.addAll(targets);
    }

    public Set<Resource> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<Resource> origins) {
        this.origins = origins;
    }

    public Set<Resource> getTargets() {
        return targets;
    }

    public void setTargets(Set<Resource> targets) {
        this.targets = targets;
    }

    public String getOriginRef() {
        return originRef;
    }

    public void setOriginRef(String originRef) {
        this.originRef = originRef;
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

    @Override
    public String toString() {
        return "ResourceDependency{" +
                "origins=" + origins +
                ", targets=" + targets +
                ", originRef='" + originRef + '\'' +
                ", targetRef='" + targetRef + '\'' +
                ", optional=" + optional +
                '}';
    }

    public void resolve(STContext context) throws InvalidDependencyException {
        try {
            if (originRef != null) {
                origins.addAll(context.findResources(originRef));
            }
            if (targetRef != null) {
                targets.addAll(context.findResources(targetRef));
            }
        } catch (InvalidQueryException e) {
            throw new InvalidDependencyException(e.getMessage(), e);
        }
        if( ( origins.isEmpty() || targets.isEmpty() ) && ! optional ) {
            throw new InvalidDependencyException("Mandatory dependency missing origins/targets: "+toString());
        }
    }
}
