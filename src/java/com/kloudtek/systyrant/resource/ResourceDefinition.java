/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidResourceDefinitionException;
import com.kloudtek.systyrant.exception.ResourceCreationException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.util.ListHashMap;
import com.kloudtek.systyrant.util.ValidateUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class ResourceDefinition implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ResourceDefinition.class);
    private FQName fqname;
    private UniqueScope uniqueScope;
    private boolean created;
    private HashMap<String, String> defaultAttrs = new HashMap<>();
    private ArrayList<Action> actions = new ArrayList<>();

    public ResourceDefinition(@NotNull FQName fqname) {
        this.fqname = fqname;
        assert isNotEmpty(fqname.getPkg());
        assert isNotEmpty(fqname.getName());
    }

    public ResourceDefinition(@NotNull String pkg, @NotNull String name) {
        fqname = new FQName(pkg, name);
    }

    public String getName() {
        return fqname.getName();
    }

    public String getPkg() {
        return fqname.getPkg();
    }

    public boolean isUnique() {
        return uniqueScope != null;
    }

    public UniqueScope getUniqueScope() {
        return uniqueScope;
    }

    public void setUniqueScope(UniqueScope uniqueScope) {
        this.uniqueScope = uniqueScope;
    }

    public void addUniqueScope(UniqueScope uniqueScope) throws InvalidResourceDefinitionException {
        if (this.uniqueScope != null && !this.uniqueScope.equals(uniqueScope)) {
            throw new InvalidResourceDefinitionException("Conflicting unique scope definition in "+fqname+" ( was "+uniqueScope+" but attempted to set to "+uniqueScope);
        }
        logger.debug("Class is unique with scope {}", uniqueScope);
        this.uniqueScope = uniqueScope;
    }

    public void addAction( Action action ) {
        actions.add(action);
    }

    @Override
    public void close() throws Exception {
    }

    public synchronized void addDefaultAttr(String name, String value) throws InvalidResourceDefinitionException {
        String curr = defaultAttrs.get(name);
        if( curr != null && value != null && ! curr.equalsIgnoreCase(value)) {
            throw new InvalidResourceDefinitionException("Conflicting default attributes in "+fqname+" ( was '"+curr+"' but attempted to set as '"+value+"'");
        }
        defaultAttrs.put(name, value);
    }

    public Resource create(STContext context, String id, String uid, Resource parent) throws ResourceCreationException {
        Resource resource = new Resource(context, this, id, uid, parent);
        try {
            for (Map.Entry<String, String> attr : defaultAttrs.entrySet()) {
                resource.set(attr.getKey(), attr.getValue());
            }
            try {
                for (Map.Entry<String, String> entry : defaultAttrs.entrySet()) {
                    resource.set(entry.getKey(), entry.getValue());
                }
                for (Action action : actions) {
                    if( action.getType() == Action.Type.INIT ) {
                        try {
                            if( action.checkExecutionRequired(context, resource) ) {
                                action.execute(context, resource);
                            }
                        } catch (STRuntimeException e) {
                            throw new ResourceCreationException(e.getMessage(),e);
                        }
                    } else {
                        resource.addAction(action);
                    }
                }
            } catch (InvalidAttributeException e) {
                throw new ResourceCreationException(e.getMessage(),e);
            }
            return resource;
        } catch (InvalidAttributeException e) {
            throw new ResourceCreationException(e.getMessage(), e);
        }
    }

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

    public void merge(ResourceDefinition resourceDefinition) throws InvalidResourceDefinitionException {
        if( ! fqname.equals(resourceDefinition.getFQName()) ) {
            throw new InvalidResourceDefinitionException("Attempted to merge two resources with different FQNames: "+fqname+" and "+resourceDefinition.getFQName());
        }
        for (Action action : resourceDefinition.actions) {
            addAction(action);
        }
        for (Map.Entry<String, String> entry : resourceDefinition.defaultAttrs.entrySet()) {
            addDefaultAttr(entry.getKey(), entry.getValue() );
        }
        addUniqueScope(resourceDefinition.uniqueScope);
    }
}
