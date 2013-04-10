/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.context.NotificationHandler;
import com.kloudtek.systyrant.context.ResourceDefinition;
import com.kloudtek.systyrant.context.ResourceDependency;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.InvalidStageException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.Host;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Resource {
    static final String SUBSCRIBE = "subscribe";
    static final String NOTIFY = "notify";

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

    void addAction(@NotNull Action action);

    <X> X getJavaImpl(Class<X> clazz);

    Host getHost();

    Host getHostOverride();

    void setHostOverride(Host hostOverride) throws STRuntimeException;

    ResourceDefinition getDefinition();

    String getName();

    String getPkg();

    boolean isExecutable();

    void setExecutable(boolean executable);

    Resource.State getState();

    Map<String, String> getAttributes();

    void setAttributes(Map<String, Object> attributes) throws InvalidAttributeException;

    Resource set(@NotNull String key, @Nullable Object valueObj) throws InvalidAttributeException;

    Resource set(@NotNull Map<String, String> attributes) throws InvalidAttributeException;

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

    STContext context();

    Host host();

    FQName getType();

    enum State {
        NEW, PREPARED, EXECUTED, CLEANEDUP, SKIP, FAILED
    }
}
