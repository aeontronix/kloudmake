/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ResourceManager extends Iterable<Resource> {
    void setContext(STContext context);

    List<ResourceDefinition> getResourceDefinitions();

    List<Resource> getResources();

    List<Resource> getChildrens(Resource resource);

    boolean isCreateAllowed();

    void setCreateAllowed(boolean createAllowed);

    Resource createResource(@NotNull String fqname, @Nullable Collection<ResourceMatcher> importPaths, @Nullable Resource parent) throws ResourceCreationException;

    Resource createResource(@NotNull FQName fqname, String id, @Nullable Resource parent, @Nullable Collection<ResourceMatcher> importPaths) throws ResourceCreationException;

    Resource createResource(@NotNull String fqname, @Nullable Collection<ResourceMatcher> importPaths) throws ResourceCreationException;

    Resource createResource(@NotNull FQName fqname, @Nullable Resource parent) throws ResourceCreationException;

    Resource createResource(@NotNull String fqname, @Nullable Resource parent) throws ResourceCreationException;

    Resource createResource(@NotNull FQName fqname) throws ResourceCreationException;

    Resource createResource(@NotNull String fqname) throws ResourceCreationException;

    Resource createResource(@NotNull Object obj) throws ResourceCreationException;

    @NotNull
    List<Resource> findResources(@Nullable String pkg, @Nullable String name, @Nullable String id);

    List<Resource> findResourcesById(@NotNull String id) throws STRuntimeException;

    void registerJavaResource(Class<?> clazz) throws InvalidResourceDefinitionException;

    void registerJavaResource(Class<?> clazz, @NotNull String fqname) throws InvalidResourceDefinitionException;

    void registerJavaResource(Class<?> clazz, @NotNull FQName fqname) throws InvalidResourceDefinitionException;

    void registerResourceDefinitions(Collection<ResourceDefinition> resourceDefinitions) throws InvalidResourceDefinitionException;

    void registerResourceDefinition(ResourceDefinition resourceDefinition) throws InvalidResourceDefinitionException;

    void close();

    void prepareForExecution() throws InvalidDependencyException;

    void resolveDependencies(boolean strict) throws InvalidDependencyException;

    boolean hasResources();

    /**
     * Find Resources using a resource query expression.
     * @param query Query expression.
     * @param baseResource Root element to search under
     * @return Matching resources.
     * @throws InvalidQueryException If the query expression was invalid.
     */
    @NotNull
    List<Resource> findResources(@NotNull String query, @Nullable Resource baseResource) throws InvalidQueryException;

    Set<ResourceDependency> getDependencies();

    void addDependency(ResourceDependency dependency);

    void removeDependency(ResourceDependency dependency);

    @NotNull
    List<Resource> findResources(@NotNull String query) throws InvalidQueryException;

    Resource createResource(@NotNull String fqname, String id, @Nullable Resource parent) throws ResourceCreationException;

    Resource createResource(@NotNull String fqname, String id) throws ResourceCreationException;
}
