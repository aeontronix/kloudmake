/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.util.ValidateUtils;
import org.jetbrains.annotations.NotNull;

import static com.kloudtek.util.StringUtils.isEmpty;

public abstract class ResourceFactory implements AutoCloseable {
    protected FQName fqname;
    protected boolean unique;
    protected boolean created;

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

    @NotNull
    public abstract Resource create(STContext context) throws ResourceCreationException;

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
