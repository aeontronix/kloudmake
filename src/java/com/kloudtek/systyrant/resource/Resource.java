/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STAction;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.service.host.Host;
import org.apache.commons.beanutils.ConvertUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.*;

import static com.kloudtek.util.StringUtils.isNotEmpty;

public class Resource {
    public static final List<String> CATTRS = Arrays.asList("id", "uid", "depends");
    private static final Logger logger = LoggerFactory.getLogger(Resource.class);
    private Map<String, String> attributes = new HashMap<>();
    private transient STContext context;
    private ResourceFactory factory;
    private ResourceDependencyRef parent;
    private final List<ResourceDependencyRef> deps = new ArrayList<>();
    private boolean executable;
    private HashSet<STAction> actions = new HashSet<>();
    private State state;
    /**
     * This contains the results of any successful verification (meaning nothing has changed). A global verification
     * will be stored as an empty string, and a specific verification will be stored as it's name.
     */
    private final HashSet<String> verification = new HashSet<>();

    public Resource(STContext context, ResourceFactory factory) {
        this.context = context;
        this.factory = factory;
        reset();
    }

    public void reset() {
        state = State.NEW;
        verification.clear();
    }

    // ----------------------------------------------------------------------
    // Dependency Management
    // ----------------------------------------------------------------------

    public List<Resource> getResolvedDeps() {
        ArrayList<Resource> list = new ArrayList<>(deps.size());
        for (ResourceDependencyRef depRef : deps) {
            Resource resource = depRef.getResource();
            if (resource != null) {
                list.add(resource);
            }
        }
        return list;
    }

    public ResourceDependencyRef addDependency(Resource resource) {
        if (resource.equals(this)) {
            throw new IllegalArgumentException("Added dependency on self: " + resource);
        }
        return addDependency(resource, false);
    }

    public ResourceDependencyRef addDependency(String ref) throws InvalidRefException {
        return addDependency(ref, false);
    }

    public ResourceDependencyRef addDependency(Resource resource, boolean optional) {
        ResourceDependencyRef depRef = new ResourceDependencyRef(this, resource, optional);
        addDependency(depRef);
        return depRef;
    }

    public ResourceDependencyRef addDependency(String ref, boolean optional) throws InvalidRefException {
        ResourceDependencyRef dep = new ResourceDependencyRef(this, ref, optional);
        addDependency(dep);
        return dep;
    }

    private void addDependency(ResourceDependencyRef depRef) {
        deps.add(depRef);
    }

    public boolean hasDependencyOn(Resource el) {
        if (!deps.isEmpty()) {
            for (ResourceDependencyRef depRef : deps) {
                Resource dep = depRef.getResource();
                if (dep == el || dep.hasDependencyOn(el)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Resource getParent() {
        if (parent != null) {
            return parent.getResource();
        } else {
            return null;
        }
    }

    public void setParent(String parent) throws InvalidRefException {
        this.parent = new ResourceDependencyRef(this, parent);
    }

    public void setParent(Resource parent) {
        this.parent = new ResourceDependencyRef(this, parent);
    }

    public List<ResourceDependencyRef> getDeps() {
        return deps;
    }

    public void resolveDepencies(boolean strict) throws InvalidDependencyException {
        try {
            for (ResourceDependencyRef dep : new ArrayList<>(deps)) {
                if (!dep.isResolved()) {
                    dep.resolve(context);
                }
            }
        } catch (InvalidDependencyException e) {
            if (strict) {
                throw e;
            }
        }
    }

    // ----------------------------------------------------------------------
    // Meta-Data retrieval
    // ----------------------------------------------------------------------

    public ResourceFactory getFactory() {
        return factory;
    }

    public String getName() {
        return factory.getName();
    }

    public String getPkg() {
        return factory.getPkg();
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    // ------------------------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------------------------

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void setAttributes(Map<String, Object> attributes) throws InvalidAttributeException {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the value for one of the resource's properties.
     *
     * @param key      Attribute key.
     * @param valueObj Attribute value.
     * @return This resource.
     */
    public Resource set(@NotNull String key, @Nullable Object valueObj) throws InvalidAttributeException {
        String value = ConvertUtils.convert(valueObj);
        key = key.toLowerCase();
        switch (key) {
            case "depends":
                deps.clear();
                try {
                    deps.add(new ResourceDependencyRef(this, value, false));
                } catch (InvalidRefException e) {
                    throw new InvalidAttributeException(e);
                }
                break;
            default:
                attributes.put(key, value);
        }
        return this;
    }

    public String get(@NotNull String key) {
        key = key.toLowerCase();
        switch (key) {
            case "depends":
                StringWriter txt = new StringWriter();
                boolean first = true;
                for (ResourceDependencyRef dep : deps) {
                    String ref = dep.getRef();
                    if (isNotEmpty(ref)) {
                        txt.append(ref);
                        if (first) {
                            txt.append(" | ");
                        }
                    }
                }
                return txt.toString();
            default:
                return attributes.get(key.toLowerCase());
        }
    }

    public void removeAttribute(@NotNull String key) {
        attributes.remove(key.toLowerCase());
    }

    public String getId() {
        return get("id");
    }

    public void setId(@Nullable String id) throws InvalidAttributeException {
        set("id", id);
    }

    public String getUid() {
        return get("uid");
    }

    public void setUid(@Nullable String uid) throws InvalidAttributeException {
        set("uid", uid);
    }

    public String generateUid() {
        StringBuilder tmp = new StringBuilder();
        Resource p = getParent();
        if (p != null) {
            tmp.append(p.generateUid()).append('.');
        }
        return tmp.append(getId()).toString();
    }

    public void addVerification(@NotNull String name) {
        synchronized (verification) {
            verification.add(name.toLowerCase());
        }
    }

    public boolean containsVerification(String verificationString) {
        synchronized (verification) {
            return verification.contains(verificationString.toLowerCase());
        }
    }

    public Set<String> getVerification() {
        synchronized (verification) {
            return Collections.unmodifiableSet(verification);
        }
    }

    // ----------------------------------------------------------------------
    // Actions
    // ----------------------------------------------------------------------

    public List<STAction> getActions() {
        return Collections.unmodifiableList(new ArrayList<>(actions));
    }

    public void addAction(STAction action) {
        actions.add(action);
    }

    // ----------------------------------------------------------------------
    // Utility functions
    // ----------------------------------------------------------------------

    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(factory.getFQName());
        String uid = getUid();
        if (uid != null) {
            tmp.append(":uid:").append(uid);
        } else {
            String id = getId();
            if (id != null) {
                tmp.append(":id:").append(id);
            }
        }
        return tmp.toString();
    }

    public Logger logger() {
        return logger;
    }

    public STContext context() {
        return context;
    }

    public Host host() throws InvalidServiceException {
        return context.host();
    }

    public FQName getType() {
        return factory.getFQName();
    }

    // ----------------------------------------------------------------------
    // Processing Methods
    // ----------------------------------------------------------------------

    public void executeActions(Stage stage, boolean postChildren) throws STRuntimeException {
        for (STAction action : actions) {
            action.execute(this, stage, postChildren);
        }
    }

    public enum State {
        NEW, PREPARED, EXECUTED, CLEANEDUP, SKIP, FAILED
    }
}
