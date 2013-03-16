/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.util.ListHashMap;
import com.kloudtek.systyrant.util.ValidateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.kloudtek.util.StringUtils.isEmpty;

public abstract class ResourceFactory implements AutoCloseable {
    protected FQName fqname;
    protected boolean unique;
    protected boolean created;
    protected HashMap<String, String> defaultAttrs = new HashMap<>();

    protected ResourceFactory(@NotNull FQName fqname) {
        this.fqname = fqname;
    }

    protected ResourceFactory(@NotNull String pkg, @NotNull String name) {
        fqname = new FQName(pkg, name);
    }

    public String getName() {
        return fqname.getName();
    }

    public String getPkg() {
        return fqname.getPkg();
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public void close() throws Exception {
    }

    protected void addDefaultAttr(String name, String value) {
        defaultAttrs.put(name, value);
    }

    public Resource create(STContext context) throws ResourceCreationException {
        Resource resource = new Resource(context, this);
        try {
            for (Map.Entry<String, String> attr : defaultAttrs.entrySet()) {
                resource.set(attr.getKey(), attr.getValue());
            }
            configure(context, resource);
            return resource;
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException(e.getMessage(), e);
        }
    }

    @NotNull
    protected abstract void configure(STContext context, Resource resource) throws ResourceCreationException;

    public FQName getFQName() {
        return fqname;
    }

    public void validate() throws InvalidResourceDefinitionException {
        if (isEmpty(fqname.getName())) {
            throw new InvalidResourceDefinitionException("Resource factory has no name: ");
        }
        if (isEmpty(fqname.getPkg())) {
            throw new InvalidResourceDefinitionException("Resource factory has no package: ");
        }
        if (!ValidateUtils.isValidId(fqname.getName())) {
            throw new InvalidResourceDefinitionException("Resource factory name is invalid: " + fqname.getName());
        }
        if (!ValidateUtils.isValidPkg(fqname.getPkg())) {
            throw new InvalidResourceDefinitionException("Resource factory package is invalid: " + fqname.getPkg());
        }
    }
}
