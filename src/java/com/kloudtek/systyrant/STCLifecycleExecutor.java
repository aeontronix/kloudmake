/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.MultipleUniqueResourcesFoundException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.util.SetHashMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.kloudtek.systyrant.Stage.*;
import static com.kloudtek.util.StringUtils.isNotEmpty;

public class STCLifecycleExecutor {
    public boolean execute() throws STRuntimeException {
        context.stage = INIT;
        context.executionLock.writeLock().lock();
        try {
            context.executing = true;
            logger.info("Systyrant context execution started");

            prepare();

            buildIndexes();

            executeResources();

            cleanup();

            boolean successful = isSuccessful();

            if (successful) {
                logger.info("Systyrant context execution completed");
            } else {
                logger.warn("Systyrant context execution completed with some errors");
            }
            return successful;
        } finally {
            context.resourceManager.setCreateAllowed(true);
            context.executing = false;
            context.clearImports();
            context.clearResourceScope();
            for (File tempFile : context.tempFiles) {
                if (!tempFile.delete()) {
                    logger.warn("Failed to delete temporary file " + tempFile.getPath() + " will attempt to delete on process exit");
                    tempFile.deleteOnExit();
                }
            }
            context.executionLock.writeLock().unlock();
            context.stage = null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(STCLifecycleExecutor.class);
    private final STContext context;

    public STCLifecycleExecutor(STContext context) {
        this.context = context;
    }

    public void prepare() throws STRuntimeException {
        context.resourceListLock.writeLock().lock();
        try {
            if (context.newLibAdded) {
                context.providersManagementService.init(context.reflections);
            }
            for (Resource resource : context.resources) {
                ((ResourceImpl) resource).reset();
            }
            context.serviceManager.start();

            executePrepareActions();

            context.resourceManager.setCreateAllowed(false);
            logger.debug("Finished PREPARE stage");
            if (context.resources.isEmpty()) {
                return;
            }

            validateResourceUniqueness();

            logger.debug("Resources: {}", context.resources);

            // add dependency on parent if missing
            for (Resource resource : new ArrayList<>(context.resources)) {
                Resource parent = resource.getParent();
                if (parent != null && !resource.getDependencies().contains(parent)) {
                    resource.addDependency(parent);
                }
            }
            // mandatory children resolution
            context.resourceManager.resolveDependencies(true);
            // build parent/child map
            context.parentChildIndex = new HashMap<>();
            for (Resource resource : context.resources) {
                if (resource.getParent() != null) {
                    context.getChildrensInternalList(resource.getParent()).add(resource);
                }
            }
            for (Resource resource : context.resources) {
                // make dependent on resource if dependent on parent (childrens excluded from this rule)
                for (Resource dep : resource.getDependencies()) {
                    if (resource.getParent() == null || !resource.getParent().equals(dep)) {
                        makeDependentOnChildren(resource, dep);
                    }
                }
                // Sort resource's actions
                ((ResourceImpl) resource).sortTasks();
            }
            // Sort according to dependencies
            ResourceSorter.sort(context.resources);
            for (Resource resource : context.resources) {
                prepareResourceForExecution((ResourceImpl) resource);
            }
            aggregateAutoNotifications();
            for (AutoNotify autoNotify : context.autoNotifications) {
                autoNotify.prepare();
            }
            ResourceSorter.bringResourcesForwardDueToNotification(context);
        } finally {
            context.resourceListLock.writeLock().unlock();
        }
    }

    private void executePrepareActions() throws STRuntimeException {
        context.stage = PREPARE;
        for (Resource res = context.getUnpreparedResource(); res != null; res = context.getUnpreparedResource()) {
            context.resourceScope.set(res);
            try {
                logger.debug("Executing PREPARE stage for : {}", res);
                ((ResourceImpl) res).executeTasks(PREPARE, false);
                ((ResourceImpl) res).setStage(PREPARE);
            } catch (STRuntimeException e) {
                if (!e.isLogged()) {
                    logger.error(e.getLocalizedMessage());
                }
                fatalFatalException(e);
                handleResourceFailure(res);
            }
            logger.debug("Resources: {}", context.resources);
            // resolve 'requires' attribute
            String requiresAttr = res.get("requires");
            if (isNotEmpty(requiresAttr)) {
                res.addRequires(requiresAttr);
            }
            for (String requiresExpr : res.getRequires()) {
                ArrayList<Resource> resolved = new RequiresExpression(res, requiresExpr).resolveRequires(context);
                ((ResourceImpl) res).assignedResolvedRequires(requiresExpr, resolved);
            }
            context.resourceManager.resolveDependencies(false);
            context.resourceScope.remove();
        }
    }

    private void validateResourceUniqueness() throws MultipleUniqueResourcesFoundException {
        HashSet<FQName> globalUnique = new HashSet<>();
        SetHashMap<Host, FQName> hostUnique = new SetHashMap<>();
        for (Resource resource : context.resources) {
            UniqueScope uniqueScope = resource.getDefinition().getUniqueScope();
            if (uniqueScope != null) {
                switch (uniqueScope) {
                    case GLOBAL:
                        if (globalUnique.contains(resource.getType())) {
                            throw new MultipleUniqueResourcesFoundException(resource);
                        } else {
                            globalUnique.add(resource.getType());
                        }
                        break;
                    case HOST:
                        HashSet<FQName> set = hostUnique.get(resource.host());
                        if (set.contains(resource.getType())) {
                            throw new MultipleUniqueResourcesFoundException(resource);
                        } else {
                            set.add(resource.getType());
                        }
                        break;
                    default:
                        throw new RuntimeException("BUG! Unknown resource scope " + uniqueScope);
                }
            }
        }
    }

    private void prepareResourceForExecution(ResourceImpl resource) throws InvalidAttributeException {
        resource.indirectDependencies = new HashSet<>(resource.dependencies);
        for (Resource dep : resource.dependencies) {
            assert dep.getIndirectDependencies() != null;
            resource.indirectDependencies.addAll(dep.getDependencies());
        }
        String subscribe = resource.get(Resource.SUBSCRIBE);
        if (isNotEmpty(subscribe)) {
            try {
                for (Resource res : context.findResources(subscribe)) {
                    res.addAutoNotification(resource);
                }
            } catch (InvalidQueryException e) {
                throw new InvalidAttributeException("Invalid query specified in " + resource.getUid() + " subscribe attribute: " + subscribe);
            }
            resource.removeAttribute(Resource.SUBSCRIBE);
        }
        String notify = resource.get(Resource.NOTIFY);
        if (isNotEmpty(notify)) {
            try {
                resource.addAutoNotifications(context.findResources(notify));
            } catch (InvalidQueryException e) {
                throw new InvalidAttributeException("Invalid query specified in " + resource.getUid() + " notify attribute: " + notify);
            }
            resource.removeAttribute(Resource.NOTIFY);
        }
    }

    private void aggregateAutoNotifications() {
        AutoNotifyList list = new AutoNotifyList(context.autoNotifications);
        for (AutoNotifyGroup group : list.groups) {
            logger.debug("Aggregating " + group);
            if (((ResourceImpl) group.target).isAggregationSupportedForNotification(group.category)) {
                while (!list.groupsMembers.get(group).isEmpty()) {
                    Set<AutoNotify> set = list.next(group);
                    AutoNotify primary = set.iterator().next();
                    set.remove(primary);
                    list.remove(primary);
                    if (!set.isEmpty()) {
                        for (AutoNotify secondary : set) {
                            primary.merge(secondary);
                            list.remove(secondary);
                            context.mergeNotification(primary, secondary);
                        }
                    }
                }
            }
        }
    }

    private void executeResources() throws STRuntimeException {
        context.stage = EXECUTE;
        logger.debug("Starting stage EXECUTE");
        // initializing context host
        context.getHost().start();
        Map<Resource, List<Resource>> parentchildrens = new HashMap<>();
        for (Map.Entry<Resource, List<Resource>> entry : context.parentToPendingChildrenMap.entrySet()) {
            parentchildrens.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Resource resource : context.resources) {
            if (resource.getHostOverride() != null) {
                resource.getHostOverride().start();
            }
            context.resourceScope.set(resource);
            // execute tasks which aren't post-children
            executeResourceTasks((ResourceImpl) resource, EXECUTE, false);
            context.resourceScope.remove();
            // checking this is the last of a set of siblings, if that is the case execute the parent's post-childrens tasks
            for (Resource parent = resource.getParent(), child = resource; parent != null; parent = parent.getParent()) {
                List<Resource> childsofchild = parentchildrens.get(child);
                List<Resource> siblings = parentchildrens.get(parent);
                if (childsofchild == null || childsofchild.isEmpty()) {
                    siblings.remove(child);
                    if (siblings.isEmpty()) {
                        context.resourceScope.set(parent);
                        executeResourceTasks((ResourceImpl) parent, EXECUTE, true);
                        context.resourceScope.remove();
                    }
                }
                child = parent;
            }
            for (AutoNotify autoNotify : context.findAutoNotificationBySource(resource)) {
                if (autoNotify.execute(resource)) {
                    context.notificationsPending.add(new Notification(autoNotify.getCategory(), resource, autoNotify.getTarget()));
                }
            }
            for (Notification notification = context.popPendingNotifications(); notification != null; notification = context.popPendingNotifications()) {
                Resource target = notification.getTarget();
                context.resourceScope.set(target);
                ((ResourceImpl) target).handleNotification(notification);
                context.resourceScope.remove();
            }
            // start children override host
            if (resource.getChildrensHostOverride() != null) {
                resource.getChildrensHostOverride().start();
            }

        }
        logger.info("Finished stage EXECUTE");
    }

    private void executeResourceTasks(ResourceImpl resource, Stage stage, boolean postChildren) throws STRuntimeException {
        if (resource.isFailed()) {
            logger.warn("Skipping {} due to a previous error", resource);
        } else if (!resource.isExecutable()) {
            logger.debug("Skipping {} ", resource);
        } else {
            try {
                resource.executeTasks(stage, postChildren);
                resource.setStage(stage);
            } catch (STRuntimeException e) {
                if (!e.isLogged()) {
                    logger.debug(e.getMessage(), e);
                    logger.error(e.getMessage());
                }
                fatalFatalException(e);
                handleResourceFailure(resource);
            }
        }
    }

    private void buildIndexes() {
        for (Resource resource : context.resourceManager) {
            Resource parent = resource.getParent();
            if (parent != null) {
                List<Resource> childrens = context.parentToPendingChildrenMap.get(parent);
                if (childrens == null) {
                    childrens = new LinkedList<>();
                    context.parentToPendingChildrenMap.put(parent, childrens);
                }
                childrens.add(resource);
            }
        }
    }

    private void fatalFatalException(Throwable e) throws STRuntimeException {
        if (e instanceof STRuntimeException && e.getCause() != null) {
            e = e.getCause();
        }
        if (context.fatalExceptions != null && !context.fatalExceptions.isEmpty()) {
            for (Class<? extends Exception> fatalException : context.fatalExceptions) {
                if (fatalException.isAssignableFrom(e.getClass())) {
                    throw new STRuntimeException("Fatal exception caught: " + e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void cleanup() {
        for (Resource resource : context.resourceManager) {
            context.setResourceScope(resource);
            try {
                ((ResourceImpl) resource).executeTasks(Stage.CLEANUP, false);
            } catch (STRuntimeException e) {
                logger.warn("Error occured during cleanup: " + e.getMessage(), e);
            }
            if (resource.getHostOverride() != null) {
                resource.getHostOverride().close();
            }
            context.clearResourceScope();
        }
        context.serviceManager.stop();
    }

    private void handleResourceFailure(Resource resource) {
        LinkedList<Resource> list = new LinkedList<>();
        list.add(resource);
        while (!list.isEmpty()) {
            ResourceImpl res = (ResourceImpl) list.removeFirst();
            res.setFailed(true);
            for (Resource dep : getDependentOn(res)) {
                list.addLast(dep);
            }
        }
    }


    private void makeDependentOnChildren(Resource resource, Resource dependency) {
        LinkedList<Resource> list = new LinkedList<>();
        list.addAll(context.getChildrensInternalList(dependency));
        while (!list.isEmpty()) {
            Resource el = list.removeFirst();
            resource.addDependency(el);
            list.addAll(context.getChildrensInternalList(el));
        }
    }

    @NotNull
    private Collection<? extends Resource> getDependentOn(Resource el) {
        ArrayList<Resource> list = new ArrayList<>();
        for (Resource resource : context.resourceManager) {
            if (resource.getDependencies().contains(el)) {
                list.add(resource);
            }
        }
        return list;
    }


    private synchronized boolean isSuccessful() {
        for (Resource resource : context.resourceManager) {
            if (resource.isFailed()) {
                return false;
            }
        }
        return true;
    }

    private class AutoNotifyList {
        private final List<AutoNotify> original;
        private LinkedList<AutoNotify> pending = new LinkedList<>();
        private HashSet<AutoNotifyGroup> groups = new HashSet<>();
        private SetHashMap<AutoNotifyGroup, AutoNotify> groupsMembers = new SetHashMap<>();
        private SetHashMap<AutoNotify, AutoNotify> depsMap = new SetHashMap<>();

        public AutoNotifyList(List<AutoNotify> original) {
            SetHashMap<AutoNotify, Resource> sourceDeps = new SetHashMap<>();
            this.original = original;
            for (AutoNotify autoNotify : original) {
                for (Resource source : autoNotify.getSources()) {
                    sourceDeps.get(autoNotify).addAll(source.getIndirectDependencies());
                }
            }
            for (AutoNotify autoNotify : original) {
                AutoNotifyGroup group = new AutoNotifyGroup(autoNotify);
                groups.add(group);
                groupsMembers.get(group).add(autoNotify);
                HashSet<Resource> deps = sourceDeps.get(autoNotify);
                for (AutoNotify otherAU : original) {
                    for (Resource source : otherAU.getSources()) {
                        if (deps.contains(source)) {
                            depsMap.get(autoNotify).add(otherAU);
                            break;
                        }
                    }
                }
            }
            pending.addAll(original);
        }

        public void remove(AutoNotify autoNotify) {
            pending.remove(autoNotify);
            AutoNotifyGroup group = new AutoNotifyGroup(autoNotify);
            groupsMembers.get(group).remove(autoNotify);
            depsMap.remove(autoNotify);
            for (HashSet<AutoNotify> set : depsMap.values()) {
                set.remove(autoNotify);
            }
        }

        public Set<AutoNotify> next(AutoNotifyGroup group) {
            HashSet<AutoNotify> set = new HashSet<>();
            for (AutoNotify autoNotify : groupsMembers.get(group)) {
                if (depsMap.get(autoNotify).isEmpty()) {
                    set.add(autoNotify);
                }
            }
            return set;
        }
    }

    private class AutoNotifyGroup {
        private Resource target;
        private String category;

        private AutoNotifyGroup(@NotNull AutoNotify autoNotify) {
            this.target = autoNotify.getTarget();
            this.category = autoNotify.getCategory();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AutoNotifyGroup that = (AutoNotifyGroup) o;

            if (!category.equals(that.category)) return false;
            if (!target.equals(that.target)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = target.hashCode();
            result = 31 * result + category.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "AutoNotifyGroup[" + target.getUid() + "~" + category + "]";
        }
    }
}
