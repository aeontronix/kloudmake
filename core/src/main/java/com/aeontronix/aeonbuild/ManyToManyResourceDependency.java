/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.InvalidDependencyException;
import com.aeontronix.aeonbuild.exception.InvalidQueryException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is used to allow the creation to other elements as a string description, which is resolved right before the
 * execution stage (so the target elements could not even exist when the ref is created).
 */
public class ManyToManyResourceDependency implements ResourceDependency {
    private Set<Resource> origins = new HashSet<>();
    private Set<Resource> targets = new HashSet<>();
    private String originRef;
    private String targetRef;
    private boolean optional;

    public ManyToManyResourceDependency() {
    }

    public ManyToManyResourceDependency(Resource origin, Resource target) {
        origins.add(origin);
        targets.add(target);
    }

    public ManyToManyResourceDependency(Resource origin, Resource target, boolean optional) {
        this(origin, target);
        this.optional = optional;
    }

    public ManyToManyResourceDependency(Resource origin, String target, boolean optional) {
        origins.add(origin);
        targetRef = target;
        this.optional = optional;
    }

    public ManyToManyResourceDependency(Resource origin, Collection<Resource> targets) {
        origins.add(origin);
        this.targets.addAll(targets);
    }

    public ManyToManyResourceDependency(Collection<Resource> origins, Resource target) {
        this.origins.addAll(origins);
        this.targets.add(target);
    }

    public ManyToManyResourceDependency(Collection<Resource> origins, Collection<Resource> targets) {
        this.origins.addAll(origins);
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

    public Set<OneToManyResourceDependency> resolve(BuildContextImpl context) throws InvalidDependencyException {
        Set<OneToManyResourceDependency> o2mSet = new HashSet<>();
        try {
            if (originRef != null) {
                origins.addAll(context.findResources(originRef));
            }
            if (targetRef != null) {
                targets.addAll(context.findResources(targetRef));
            }
            if (origins.isEmpty() && !optional) {
                throw new InvalidDependencyException("Mandatory dependency has no origins: " + toString());
            }
            if (targets.isEmpty() && !optional) {
                throw new InvalidDependencyException("Mandatory dependency has no targets: " + toString());
            }
        } catch (InvalidQueryException e) {
            throw new InvalidDependencyException(e.getMessage(), e);
        }
        for (Resource origin : origins) {
            HashSet<Resource> tlist = new HashSet<>(targets);
            tlist.remove(origin);
            o2mSet.add(new OneToManyResourceDependency(origin, tlist));
        }
        return o2mSet;
    }
}
