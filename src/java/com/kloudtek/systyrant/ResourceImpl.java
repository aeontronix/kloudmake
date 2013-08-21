/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.util.VariableMap;
import org.apache.commons.beanutils.ConvertUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResourceImpl implements Resource {
    private static final Logger logger = LoggerFactory.getLogger(Resource.class);
    private Map<String, String> attributes = new HashMap<>();
    private transient STContext context;
    private ResourceDefinition definition;
    private Resource parent;
    private boolean executable = true;
    private boolean failed;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final HashSet<Resource> childrens = new HashSet<>();
    private ArrayList<Task> tasks = new ArrayList<>();
    private Stage stage;
    private Host hostOverride;
    private Host childrensHostOverride;
    private final String sourceUrl;
    /**
     * This contains the results of any successful verification (meaning nothing has changed). A global verification
     * will be stored as an empty string, and a specific verification will be stored as it's name.
     */
    private final HashSet<String> verification = new HashSet<>();
    private final HashSet<Object> javaImpls = new HashSet<>();
    private final HashSet<NotificationHandler> notificationHandlers = new HashSet<>();
    final HashSet<Resource> dependencies = new HashSet<>();
    HashSet<Resource> indirectDependencies;
    final HashSet<Resource> dependents = new HashSet<>();
    final HashMap<String, List<Resource>> requires = new HashMap<>();
    final VariableMap variables = new VariableMap(this);

    public ResourceImpl(STContext context, ResourceDefinition definition, String id, String uid, Resource parent) {
        this.context = context;
        this.definition = definition;
        this.parent = parent;
        attributes.put("id", id);
        attributes.put("uid", uid);
        if (parent != null) {
            ((ResourceImpl) parent).childrens.add(this);
        }
        reset();
        sourceUrl = context.getSourceUrl();
    }

    public void reset() {
        executable = true;
        stage = Stage.INIT;
        verification.clear();
        indirectDependencies = null;
        dependencies.clear();
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    // ----------------------------------------------------------------------
    // Dependency Management
    // ----------------------------------------------------------------------

    @Override
    public Set<Resource> getChildrens() {
        return Collections.unmodifiableSet(childrens);
    }

    @Override
    public Set<Resource> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    @Override
    public ResourceDependency addDependencies(Collection<Resource> resources) {
        return addDependencies(resources, false);
    }

    public Lock rlock() {
        return lock.readLock();
    }

    public Lock wlock() {
        return lock.writeLock();
    }

    @Override
    public ResourceDependency addDependencies(Collection<Resource> resources, boolean optional) {
        if (resources.contains(this)) {
            throw new IllegalArgumentException("Added dependencies contain self: " + resources);
        }
        ResourceDependency depRef = new OneToManyResourceDependency(this, resources, optional);
        context.getResourceManager().addDependency(depRef);
        return depRef;
    }

    @Override
    public ResourceDependency addDependency(Resource resource) {
        return addDependency(resource, false);
    }

    @Override
    public ResourceDependency addDependency(String ref) throws InvalidRefException {
        return addDependency(ref, false);
    }

    @Override
    public ResourceDependency addDependency(Resource resource, boolean optional) {
        if (resource.equals(this)) {
            throw new IllegalArgumentException("Added dependency on self: " + resource);
        }
        ResourceDependency depRef = new OneToManyResourceDependency(this, resource, optional);
        context.getResourceManager().addDependency(depRef);
        return depRef;
    }

    @Override
    public ResourceDependency addDependency(String ref, boolean optional) throws InvalidRefException {
        ResourceDependency dep = new OneToManyResourceDependency(this, ref, optional);
        context.getResourceManager().addDependency(dep);
        return dep;
    }

    @Override
    public Resource getParent() {
        return parent;
    }

    @Override
    public Set<String> getRequires() {
        return Collections.unmodifiableSet(requires.keySet());
    }

    @Override
    @Nullable
    public List<Resource> getResolvedRequires(String expr) {
        return requires.get(expr);
    }

    @Override
    public void addRequires(String requiresExpr) {
        if (requires.get(requiresExpr) == null) {
            requires.put(requiresExpr, null);
        }
    }

    @Override
    public void removeRequires(String requiresExpr) {
        requires.remove(requiresExpr);
    }

    public void assignedResolvedRequires(String requiresExpr, List<Resource> resources) {
        requires.put(requiresExpr, resources);
    }

    @Override
    public Set<Resource> getIndirectDependencies() {
        return indirectDependencies;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    // ----------------------------------------------------------------------
    // Notifications
    // ----------------------------------------------------------------------

    /**
     * Add a notification handler to this resource. This can only be done before the {@link com.kloudtek.systyrant.Stage#EXECUTE} stage.
     *
     * @param notificationHandler Handler to add to this resource.
     * @throws InvalidStageException If attempted to add the handler at EXECUTE or later stage.
     */
    @Override
    public void addNotificationHandler(NotificationHandler notificationHandler) throws InvalidStageException {
        synchronized (notificationHandlers) {
            if (context.getStage() != null && context.getStage().ordinal() < Stage.EXECUTE.ordinal()) {
                throw new InvalidStageException("Notification handlers can only be added prior to execution stages");
            }
            notificationHandlers.add(notificationHandler);
        }
    }

    // ----------------------------------------------------------------------
    // Actions
    // ----------------------------------------------------------------------

    @Override
    public void addTask(@NotNull Task task) {
        tasks.add(task);
    }

    public synchronized void sortTasks() {
        Collections.sort(tasks);
    }

    // ----------------------------------------------------------------------
    // Meta-Data retrieval
    // ----------------------------------------------------------------------

    public synchronized void addJavaImpl(Object obj) {
        javaImpls.add(obj);
    }

    @Override
    public synchronized <X> X getJavaImpl(Class<X> clazz) {
        for (Object javaImpl : javaImpls) {
            if (javaImpl.getClass().isAssignableFrom(clazz)) {
                return clazz.cast(javaImpl);
            }
        }
        return null;
    }

    @Override
    public Host getHost() {
        if (hostOverride != null) {
            return hostOverride;
        } else if (parent != null && parent.getChildrensHostOverride() != null) {
            return parent.getChildrensHostOverride();
        } else if (parent != null) {
            return parent.getHost();
        } else {
            return context.getHost();
        }
    }

    @Override
    public synchronized Host getHostOverride() {
        return hostOverride;
    }

    @Override
    public synchronized void setHostOverride(Host hostOverride) throws STRuntimeException {
        if (this.hostOverride != null && this.hostOverride != hostOverride) {
            this.hostOverride.close();
        }
        if (context.getStage() != null && context.getStage().ordinal() >= Stage.EXECUTE.ordinal()) {
            throw new STRuntimeException("Host overrides cannot be changed after the prepare stage");
        }
        this.hostOverride = hostOverride;
        if (hostOverride != null) {
            context.inject(hostOverride);
        }
    }

    @Override
    public Host getChildrensHostOverride() {
        return childrensHostOverride;
    }

    @Override
    public void setChildrensHostOverride(Host childrensHostOverride) throws STRuntimeException {
        if (this.childrensHostOverride != null && this.childrensHostOverride != childrensHostOverride) {
            this.childrensHostOverride.close();
        }
        if (context.getStage() != null && context.getStage().ordinal() >= Stage.EXECUTE.ordinal()) {
            throw new STRuntimeException("Children host overrides cannot be changed after the prepare stage");
        }
        this.childrensHostOverride = childrensHostOverride;
        if (childrensHostOverride != null) {
            context.inject(childrensHostOverride);
        }
    }

    @Override
    public ResourceDefinition getDefinition() {
        return definition;
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public String getPkg() {
        return definition.getPkg();
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ------------------------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------------------------

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public void setAttributes(Map<Object, Object> attributes) throws InvalidAttributeException {
        logger.debug("Setting {}'s attributes to {}", definition.getFQName(), attributes);
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            set(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Sets the value for one of the resource's properties.
     *
     * @param key      Attribute key.
     * @param valueObj Attribute value.
     * @return This resource.
     */
    @Override
    public Resource set(@NotNull String key, @Nullable Object valueObj) throws InvalidAttributeException {
        key = key.trim().toLowerCase();
        if (key.equalsIgnoreCase("id") || key.equalsIgnoreCase("uid")) {
            throw new InvalidAttributeException("attribute id cannot be modified");
        }
        String value = ConvertUtils.convert(valueObj);
        logger.debug("Setting {}'s attribute {} to {}", definition.getFQName(), key, value);
        attributes.put(key, value);
        return this;
    }

    @Override
    public Resource set(@NotNull Map<Object, Object> attributes) throws InvalidAttributeException {
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            set(entry.getKey().toString(), entry.getValue());
        }
        return this;
    }

    @Override
    public String get(@NotNull String key) {
        return attributes.get(key.toLowerCase().toLowerCase());
    }

    @Override
    public void removeAttribute(@NotNull String key) {
        if (key.equalsIgnoreCase("id")) {
            throw new IllegalArgumentException("attribute id cannot be removed");
        }
        attributes.remove(key.toLowerCase());
    }

    @Override
    public Map<String, Object> getVars() {
        synchronized (variables) {
            return Collections.unmodifiableMap(variables);
        }
    }

    @Override
    public Object getVar(String name) {
        synchronized (variables) {
            return variables.get(name);
        }
    }

    @Override
    public Object getVar(String name, boolean inResourceOnly) {
        if (inResourceOnly) {
            synchronized (variables) {
                if (variables.containsKey(name)) {
                    return variables.get(name);
                } else {
                    return null;
                }
            }
        } else {
            return getVar(name);
        }
    }

    @Override
    public void setVar(String name, Object value) {
        synchronized (variables) {
            variables.put(name, value);
        }
    }

    @Override
    public void removeVar(String name) {
        synchronized (variables) {
            variables.remove(name);
        }
    }

    @Override
    public String getId() {
        return get("id");
    }

    void setId(String value) {
        attributes.put("id", value);
        logger.debug("setting id {}", value);
    }

    @Override
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
            tmp.append(((ResourceImpl) p).generateUid()).append('.');
        }
        return tmp.append(getId()).toString();
    }

    @Override
    public void addVerification(@NotNull String name) {
        synchronized (verification) {
            verification.add(name.toLowerCase());
        }
    }

    @Override
    public boolean containsVerification(String verificationString) {
        synchronized (verification) {
            return verification.contains(verificationString.toLowerCase());
        }
    }

    @Override
    public Set<String> getVerification() {
        synchronized (verification) {
            return Collections.unmodifiableSet(verification);
        }
    }

    @Override
    public void addAutoNotification(Resource resource) {
        context.addAutoNotification(new AutoNotify(this, resource, null));
    }

    @Override
    public void addAutoNotifications(Collection<Resource> resources) {
        for (Resource resource : resources) {
            addAutoNotification(resource);
        }
    }

    // ----------------------------------------------------------------------
    // Utility functions
    // ----------------------------------------------------------------------

    public String toString() {
        return definition.getFQName().toString() + ":" + getUid();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public STContext context() {
        return context;
    }

    @Override
    public Host host() {
        return getHost();
    }

    @Override
    public FQName getType() {
        return definition.getFQName();
    }

    // ----------------------------------------------------------------------
    // Processing Methods
    // ----------------------------------------------------------------------

    public void executeTasks(Stage stage, boolean postChildren) throws STRuntimeException {
        HashSet<String> supportedAlternatives = new HashSet<>();
        HashSet<String> requiredAlternatives = new HashSet<>();
        for (Task task : tasks) {
            if (task.getStage() == stage && task.isPostChildren() == postChildren) {
                String alternative = task.getAlternative();
                if (alternative != null) {
                    requiredAlternatives.add(alternative);
                }
                if (task.supports(context, this)) {
                    if (alternative != null) {
                        supportedAlternatives.add(alternative);
                    }
                    if (task.checkExecutionRequired(context, this)) {
                        task.execute(context, this);
                    }
                }
            }
        }
        for (String requiredAlternative : requiredAlternatives) {
            if (!supportedAlternatives.contains(requiredAlternative)) {
                StringBuilder msg = new StringBuilder("Unable to find ").append(requiredAlternative);
                if (!requiredAlternative.isEmpty()) {
                    msg.append(' ');
                }
                msg.append("alternative for ").append(getType());
                throw new MissingAlternativeException(msg.toString());
            }
        }
    }

    public void handleNotification(Notification notification) throws STRuntimeException {
        synchronized (notificationHandlers) {
            for (NotificationHandler handler : notificationHandlers) {
                if (handler.isSameCategory(handler.getCategory()) && (!handler.isOnlyIfAfter() || stage.ordinal() >= Stage.EXECUTE.ordinal())) {
                    handler.handleNotification(notification);
                }
            }
        }
    }

    public boolean isReOrderRequiredForNotification(@NotNull String category) {
        for (NotificationHandler notificationHandler : notificationHandlers) {
            if (notificationHandler.getCategory().equalsIgnoreCase(category) && notificationHandler.isReorder()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAggregationSupportedForNotification(@NotNull String category) {
        boolean match = false;
        for (NotificationHandler notificationHandler : notificationHandlers) {
            if (notificationHandler.getCategory().equalsIgnoreCase(category)) {
                if (notificationHandler.isAggregate()) {
                    // Notification handler supports aggregation
                    match = true;
                } else if (match) {
                    // There's a notification handler on that category that doesn't support aggregation
                    return false;
                }
            }
        }
        return match;
    }
}
