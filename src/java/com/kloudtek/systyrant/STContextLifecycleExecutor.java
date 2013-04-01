/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.MultipleUniqueResourcesFoundException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.resource.*;
import com.kloudtek.systyrant.util.SetHashMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.kloudtek.systyrant.Resource.State.FAILED;
import static com.kloudtek.systyrant.Resource.State.PREPARED;
import static com.kloudtek.systyrant.Stage.EXECUTE;
import static com.kloudtek.util.StringUtils.isNotEmpty;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 01/04/13
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public class STContextLifecycleExecutor {
    private static final Logger logger = LoggerFactory.getLogger(STContextLifecycleExecutor.class);
    private final STContext context;
    private final STContextData data;

    public STContextLifecycleExecutor(STContext context, STContextData data) {
        this.context = context;
        this.data = data;
    }

    public void prepare() throws STRuntimeException {
        data.resourceListLock.writeLock().lock();
        try {
            data.host.start();
            if (data.newLibAdded) {
                data.providersManagementService.init(data.reflections);
            }
            for (Resource resource : data.resources) {
                resource.reset();
            }
            data.serviceManager.start();
            for (Resource res = data.getUnpreparedResource(); res != null; res = data.getUnpreparedResource()) {
                data.resourceScope.set(res);
                try {
                    res.executeActions(Stage.PREPARE, false);
                    res.setState(PREPARED);
                } catch (STRuntimeException e) {
                    if (!e.isLogged()) {
                        logger.error(e.getLocalizedMessage());
                    }
                    fatalFatalException(e);
                    handleResourceFailure(res);
                }
                // resolve 'requires' attribute
                String requiresAttr = res.get("requires");
                if (isNotEmpty(requiresAttr)) {
                    res.addRequires(requiresAttr);
                }
                for (String requiresExpr : res.getRequires()) {
                    ArrayList<Resource> resolved = new RequiresExpression(res, requiresExpr).resolveRequires(context);
                    res.assignedResolvedRequires(requiresExpr, resolved);
                }
                data.resourceManager.resolveDependencies(false);
                data.resourceScope.remove();
            }
            data.resourceManager.setCreateAllowed(false);

            if (data.resources.isEmpty()) {
                return;
            }

            validateResourceUniqueness();

            // add dependency on parent if missing
            for (Resource resource : new ArrayList<>(data.resources)) {
                Resource parent = resource.getParent();
                if (parent != null && !resource.getDependencies().contains(parent)) {
                    resource.addDependency(parent);
                }
            }
            // mandatory children resolution
            data.resourceManager.resolveDependencies(true);
            // build parent/child map
            data.parentChildIndex = new HashMap<>();
            for (Resource resource : data.resources) {
                if (resource.getParent() != null) {
                    data.getChildrensInternalList(resource.getParent()).add(resource);
                }
            }
            for (Resource resource : data.resources) {
                // make dependent on resource if dependent on parent (childrens excluded from this rule)
                for (Resource dep : resource.getDependencies()) {
                    if (resource.getParent() == null || !resource.getParent().equals(dep)) {
                        makeDependentOnChildren(resource, dep);
                    }
                }
                // Sort resource's actions
                resource.sortActions();
            }
            // Sort according to dependencies
            ResourceSorter.sort(data.resources);
            for (Resource resource : data.resources) {
                prepareResourceForExecution(resource);
            }
            aggregateAutoNotifications();
            for (AutoNotify autoNotify : data.autoNotifications) {
                autoNotify.prepare();
            }
            ResourceSorter.bringResourcesForwardDueToNotification(data);
        } finally {
            data.resourceListLock.writeLock().unlock();
        }
    }

    private void validateResourceUniqueness() throws MultipleUniqueResourcesFoundException {
        HashSet<FQName> globalUnique = new HashSet<>();
        SetHashMap<Host, FQName> hostUnique = new SetHashMap<>();
        for (Resource resource : data.resources) {
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

    private void prepareResourceForExecution(Resource resource) throws InvalidAttributeException {
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
        AutoNotifyList list = new AutoNotifyList(data.autoNotifications);
        for (AutoNotifyGroup group : list.groups) {
            logger.debug("Aggregating " + group);
            if (group.target.isAggregationSupportedForNotification(group.category)) {
                while (!list.groupsMembers.get(group).isEmpty()) {
                    Set<AutoNotify> set = list.next(group);
                    AutoNotify primary = set.iterator().next();
                    set.remove(primary);
                    list.remove(primary);
                    if (!set.isEmpty()) {
                        for (AutoNotify secondary : set) {
                            primary.merge(secondary);
                            list.remove(secondary);
                            data.mergeNotification(primary, secondary);
                        }
                    }
                }
            }
        }
    }

    public boolean execute() throws STRuntimeException {
        data.executionLock.writeLock().lock();
        try {
            data.executing = true;
            logger.info("Systyrant context execution started");

            prepare();

            buildIndexes();

            executeResources(EXECUTE);

            cleanup();

            boolean successful = isSuccessful();

            if (successful) {
                logger.info("Systyrant context execution completed");
            } else {
                logger.warn("Systyrant context execution completed with some errors");
            }
            return successful;
        } finally {
            data.resourceManager.setCreateAllowed(true);
            data.executing = false;
            context.clearImports();
            data.clearResourceScope();
            for (File tempFile : data.tempFiles) {
                if (!tempFile.delete()) {
                    logger.warn("Failed to delete temporary file " + tempFile.getPath() + " will attempt to delete on process exit");
                    tempFile.deleteOnExit();
                }
            }
            data.executionLock.writeLock().unlock();
        }

    }

    private void executeResources(Stage stage) throws STRuntimeException {
        Map<Resource, List<Resource>> parentchildrens = new HashMap<>();
        for (Map.Entry<Resource, List<Resource>> entry : data.parentToPendingChildrenMap.entrySet()) {
            parentchildrens.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Resource resource : data.resourceManager) {
            data.resourceScope.set(resource);
            executeResourceActions(resource, stage, false);
            data.resourceScope.remove();
            for (Resource parent = resource.getParent(), child = resource; parent != null; parent = parent.getParent()) {
                List<Resource> childsofchild = parentchildrens.get(child);
                List<Resource> siblings = parentchildrens.get(parent);
                if (childsofchild == null || childsofchild.isEmpty()) {
                    siblings.remove(child);
                    if (siblings.isEmpty()) {
                        data.resourceScope.set(parent);
                        executeResourceActions(parent, stage, true);
                        data.resourceScope.remove();
                    }
                }
                child = parent;
            }
            for (AutoNotify autoNotify : data.findAutoNotificationBySource(resource)) {
                if (autoNotify.execute(resource)) {
                    data.notificationsPending.add(new Notification(autoNotify.getCategory(), resource, autoNotify.getTarget()));
                }
            }
            for (Notification notification = data.popPendingNotifications(); notification != null; notification = data.popPendingNotifications()) {
                Resource target = notification.getTarget();
                data.resourceScope.set(target);
                target.handleNotification(notification);
                data.resourceScope.remove();
            }
        }
    }

    private void executeResourceActions(Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
        switch (resource.getState()) {
            case FAILED:
                logger.warn("Skipping {} due to a previous error", resource);
                break;
            case SKIP:
                logger.debug("Skipping {} ", resource);
                break;
            default:
                try {
                    resource.executeActions(stage, postChildren);
                    resource.setState(stage.getState());
                } catch (STRuntimeException e) {
                    if (!e.isLogged()) {
                        logger.debug(e.getMessage(), e);
                        logger.error(e.getMessage());
                    }
                    fatalFatalException(e);
                    handleResourceFailure(resource);
                }
                break;
        }
    }

    private void buildIndexes() {
        for (Resource resource : data.resourceManager) {
            Resource parent = resource.getParent();
            if (parent != null) {
                List<Resource> childrens = data.parentToPendingChildrenMap.get(parent);
                if (childrens == null) {
                    childrens = new LinkedList<>();
                    data.parentToPendingChildrenMap.put(parent, childrens);
                }
                childrens.add(resource);
            }
        }
    }

    private void fatalFatalException(Throwable e) throws STRuntimeException {
        if (e instanceof STRuntimeException && e.getCause() != null) {
            e = e.getCause();
        }
        if (data.fatalExceptions != null && !data.fatalExceptions.isEmpty()) {
            for (Class<? extends Exception> fatalException : data.fatalExceptions) {
                if (fatalException.isAssignableFrom(e.getClass())) {
                    throw new STRuntimeException("Fatal exception caught: " + e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void cleanup() {
        for (Resource resource : data.resourceManager) {
            data.setResourceScope(resource);
            try {
                resource.executeActions(Stage.CLEANUP, false);
            } catch (STRuntimeException e) {
                logger.warn("Error occured during cleanup: " + e.getMessage(), e);
            }
            data.clearResourceScope();
        }
        data.serviceManager.stop();
        data.host.stop();
    }

    private void handleResourceFailure(Resource resource) {
        LinkedList<Resource> list = new LinkedList<>();
        list.add(resource);
        while (!list.isEmpty()) {
            Resource el = list.removeFirst();
            el.setState(FAILED);
            for (Resource dep : getDependentOn(el)) {
                if (data.failurePropagationStates.contains(dep.getState())) {
                    list.addLast(dep);
                }
            }
        }
    }


    private void makeDependentOnChildren(Resource resource, Resource dependency) {
        LinkedList<Resource> list = new LinkedList<>();
        list.addAll(data.getChildrensInternalList(dependency));
        while (!list.isEmpty()) {
            Resource el = list.removeFirst();
            resource.addDependency(el);
            list.addAll(data.getChildrensInternalList(el));
        }
    }

    @NotNull
    private Collection<? extends Resource> getDependentOn(Resource el) {
        ArrayList<Resource> list = new ArrayList<>();
        for (Resource resource : data.resourceManager) {
            if (resource.getDependencies().contains(el)) {
                list.add(resource);
            }
        }
        return list;
    }


    private synchronized boolean isSuccessful() {
        for (Resource resource : data.resourceManager) {
            if (resource.getState() == FAILED) {
                return false;
            }
        }
        return true;
    }

    private class AutoNotifyList {
        private final List<AutoNotify> original;
        private LinkedList<AutoNotify> pending = new LinkedList();
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
                System.out.println("FINDING DEPS OF " + autoNotify);
                HashSet<Resource> deps = sourceDeps.get(autoNotify);
                for (AutoNotify otherAU : original) {
                    System.out.println("CANDIDATE " + otherAU);
                    for (Resource source : otherAU.getSources()) {
                        System.out.println("COMPARE " + source + " WITH " + deps);
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
