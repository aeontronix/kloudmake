/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.DepencyNotFoundException;
import com.kloudtek.systyrant.exception.InvalidDependencyException;
import com.kloudtek.systyrant.exception.InvalidRefException;

import java.util.Iterator;
import java.util.List;

/**
 * This is used to allow the creation to other elements as a string description, which is resolved right before the
 * execution stage (so the target elements could not even exist when the ref is created).
 */
public class ResourceDependencyRef extends ResourceRef {
    private Resource resource;
    private boolean optional;
    private Resource origin;

    public ResourceDependencyRef(Resource origin, Resource el) {
        this(origin, el, false);
    }

    public ResourceDependencyRef(Resource origin, String ref) throws InvalidRefException {
        this(origin, ref, false);
    }

    public ResourceDependencyRef(Resource origin, Resource el, boolean optional) {
        this.origin = origin;
        this.resource = el;
        this.optional = optional;
    }

    public ResourceDependencyRef(Resource origin, String ref, boolean optional) throws InvalidRefException {
        super(ref);
        this.origin = origin;
        this.optional = optional;
    }

    public Resource getResource() {
        return resource;
    }

    public synchronized void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean isResolved() {
        return resource != null;
    }

    public synchronized void resolve(STContext context) throws InvalidDependencyException {
        if (isResolved()) {
            return;
        }
        try {
            List<Resource> resources = resolveMultiple(context, origin);
            // an resource can't be dependent on itself, silently remove it
            resources.remove(origin);
            Iterator<Resource> iterator = resources.iterator();
            if (!iterator.hasNext()) {
                if (optional) {
                    return;
                } else {
                    origin.setState(Resource.State.FAILED);
                    throw new DepencyNotFoundException("Unable to find dependency for reference: " + getRef());
                }
            }
            resource = iterator.next();
            while (iterator.hasNext()) {
                origin.addDependency(iterator.next());
            }
        } catch (InvalidRefException e) {
            throw new InvalidDependencyException(e);
        }
    }
}
