/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.exception.InvalidAttributeException;
import com.aeontronix.aeonbuild.exception.InvalidRefException;
import com.aeontronix.aeonbuild.exception.InvalidStageException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.host.Host;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Resource {
    String SUBSCRIBE = "subscribe";
    String NOTIFY = "notify";

    Set<Resource> getChildrens();

    Set<Resource> getDependencies();

    ResourceDependency addDependencies(Collection<Resource> resources);

    ResourceDependency addDependencies(Collection<Resource> resources, boolean optional);

    ResourceDependency addDependency(Resource resource);

    ResourceDependency addDependency(String ref) throws InvalidRefException;

    ResourceDependency addDependency(Resource resource, boolean optional);

    ResourceDependency addDependency(String ref, boolean optional) throws InvalidRefException;

    Resource getParent();

    Set<String> getRequires();

    @Nullable
    List<Resource> getResolvedRequires(String expr);

    void addRequires(String requiresExpr);

    void removeRequires(String requiresExpr);

    Set<Resource> getIndirectDependencies();

    void addNotificationHandler(NotificationHandler notificationHandler) throws InvalidStageException;

    void addTask(@NotNull Task task);

    <X> X getJavaImpl(Class<X> clazz);

    Host getHost();

    Host getHostOverride();

    void setHostOverride(Host hostOverride) throws KMRuntimeException;

    Host getChildrensHostOverride();

    void setChildrensHostOverride(Host childrensHostOverride) throws KMRuntimeException;

    ResourceDefinition getDefinition();

    String getName();

    String getPkg();

    boolean isExecutable();

    void setExecutable(boolean executable);

    Stage getStage();

    Map<String, String> getAttributes();

    void setAttributes(Map<Object, Object> attributes) throws InvalidAttributeException;

    Resource set(@NotNull String key, @Nullable Object valueObj) throws InvalidAttributeException;

    Resource set(@NotNull Map<Object, Object> attributes) throws InvalidAttributeException;

    String get(@NotNull String key);

    void removeAttribute(@NotNull String key);

    String getId();

    String getUid();

    void addVerification(@NotNull String name);

    boolean containsVerification(String verificationString);

    Set<String> getVerification();

    void addAutoNotification(Resource resource);

    void addAutoNotifications(Collection<Resource> resources);

    Logger logger();

    BuildContextImpl context();

    Host host();

    FQName getType();

    boolean isFailed();

    Map<String, Object> getVars();

    Object getVar(String name);

    Object getVar(String name, boolean inResourceOnly);

    void setVar(String name, Object value);

    void removeVar(String name);

    String getSourceUrl();
}
