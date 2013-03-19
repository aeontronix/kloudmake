/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.Stage;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.util.ListHashMap;
import org.apache.commons.beanutils.ConvertUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Resource {
    private static final Logger logger = LoggerFactory.getLogger(Resource.class);
    private Map<String, String> attributes = new HashMap<>();
    private transient STContext context;
    private ResourceFactory factory;
    private Resource parent;
    private boolean executable;

    private ListHashMap<Stage, Action> actions = new ListHashMap<>();
    private ListHashMap<Stage, Action> postChildrenActions = new ListHashMap<>();

    private State state;
    private Host hostOverride;
    /**
     * This contains the results of any successful verification (meaning nothing has changed). A global verification
     * will be stored as an empty string, and a specific verification will be stored as it's name.
     */
    private final HashSet<String> verification = new HashSet<>();
    private final HashSet<Object> javaImpls = new HashSet<>();
    final HashSet<Resource> dependencies = new HashSet<>();
    final HashSet<Resource> indirectDependencies = new HashSet<>();
    final HashSet<Resource> dependents = new HashSet<>();

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

    public Set<Resource> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public ResourceDependency addDependencies(Collection<Resource> resources) {
        return addDependencies(resources,false);
    }

    public ResourceDependency addDependencies(Collection<Resource> resources, boolean optional ) {
        if (resources.contains(this)) {
            throw new IllegalArgumentException("Added dependencies contain self: " + resources);
        }
        ResourceDependency depRef = new OneToManyResourceDependency(this, resources, optional);
        context.getResourceManager().addDependency(depRef);
        return depRef;
    }

    public ResourceDependency addDependency(Resource resource) {
        return addDependency(resource, false);
    }

    public ResourceDependency addDependency(String ref) throws InvalidRefException {
        return addDependency(ref, false);
    }

    public ResourceDependency addDependency(Resource resource, boolean optional) {
        if (resource.equals(this)) {
            throw new IllegalArgumentException("Added dependency on self: " + resource);
        }
        ResourceDependency depRef = new OneToManyResourceDependency(this, resource, optional);
        context.getResourceManager().addDependency(depRef);
        return depRef;
    }

    public ResourceDependency addDependency(String ref, boolean optional) throws InvalidRefException {
        ResourceDependency dep = new OneToManyResourceDependency(this, ref, optional);
        context.getResourceManager().addDependency(dep);
        return dep;
    }

    public Resource getParent() {
        return parent;
    }

    public void setParent(Resource parent) {
        this.parent = parent;
    }

    // ----------------------------------------------------------------------
    // Actions
    // ----------------------------------------------------------------------

    public synchronized List<Action> getActions(@NotNull Stage stage, boolean postChildren) {
        return Collections.unmodifiableList(getActionsInternal(stage, postChildren));
    }

    public void addAction(@NotNull Stage stage, Action action) {
        addAction(stage,false,action);
    }

    public synchronized void addAction(@NotNull Stage stage, boolean postChildren, Action action) {
        List<Action> list = getActionsInternal(stage, postChildren);
        if (!list.contains(action)) {
            list.add(action);
        }
    }

    public synchronized void addAction(ListHashMap<Stage, Action> actions, ListHashMap<Stage, Action> postChildrenActions) {
        for (Stage stage : Stage.values()) {
            this.actions.get(stage).addAll(actions.get(stage));
            this.postChildrenActions.get(stage).addAll(postChildrenActions.get(stage));
        }
    }


    public synchronized boolean removeAction(@NotNull Stage stage, boolean postChildren, Action action) {
        return getActionsInternal(stage, postChildren).remove(action);
    }

    private List<Action> getActionsInternal(Stage stage, boolean postChildren) {
        return postChildren ? postChildrenActions.get(stage) : actions.get(stage);
    }

    public synchronized void sortActions() {
        for (Stage stage : Stage.values()) {
            Collections.sort(actions.get(stage));
            Collections.sort(postChildrenActions.get(stage));
        }
    }

    // ----------------------------------------------------------------------
    // Meta-Data retrieval
    // ----------------------------------------------------------------------

    public synchronized void addJavaImpl(Object obj) {
        javaImpls.add(obj);
    }

    public synchronized <X> X getJavaImpl( Class<X> clazz ) {
        for (Object javaImpl : javaImpls) {
            if( javaImpl.getClass().isAssignableFrom(clazz) ) {
                return clazz.cast(javaImpl);
            }
        }
        return null;
    }

    public Host getHost() {
        if (hostOverride != null) {
            return hostOverride;
        } else if (parent != null) {
            return parent.getHost();
        } else {
            return context.getHost();
        }
    }

    public synchronized Host getHostOverride() {
        return hostOverride;
    }

    public synchronized void setHostOverride(Host hostOverride) throws STRuntimeException {
        if (this.hostOverride != null && this.hostOverride != hostOverride) {
            this.hostOverride.stop();
        }
        this.hostOverride = hostOverride;
        if (hostOverride != null) {
            context.inject(hostOverride);
            synchronized (hostOverride) {
                if (!hostOverride.isStarted()) {
                    hostOverride.start();
                }
            }
        }
    }

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
        attributes.put(key.toLowerCase(), value);
        return this;
    }

    public String get(@NotNull String key) {
        return attributes.get(key.toLowerCase().toLowerCase());
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
        List<Action> list = getActionsInternal(stage, postChildren);
        logger.debug("Executing {} actions for stage {} : {}",postChildren?"postchildren":"",stage,list);
        for (Action action : list) {
            if (action instanceof SyncAction) {
                logger.debug("Executing verification stage of Sync Action {}",action);
                boolean verified = ((SyncAction) action).verify(context, this, stage, postChildren);
                logger.debug("Sync Action {} returned {}",action,verified);
                if (!verified) {
                    logger.debug("Executing Sync Action {}",action,verified);
                    action.execute(context, this, stage, postChildren);
                }
            } else {
                logger.debug("Executing Action {}",action);
                action.execute(context, this, stage, postChildren);
            }
            logger.debug("Finished executing Action {}",action);
        }
    }

    public enum State {
        NEW, PREPARED, EXECUTED, CLEANEDUP, SKIP, FAILED
    }
}
