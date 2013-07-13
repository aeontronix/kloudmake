/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.Inject;
import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.dsl.DSLScriptingEngineFactory;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.host.LocalHost;
import com.kloudtek.systyrant.provider.ProviderManager;
import com.kloudtek.systyrant.provider.ProvidersManagementService;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.util.ListHashMap;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static javax.script.ScriptContext.ENGINE_SCOPE;

/**
 * This is the "brains" of SysTyrant, which contains the application's state
 */
public class STContext implements AutoCloseable {
    private static final HashMap<String, String> scriptingSupport = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(STContext.class);
    static ThreadLocal<STContext> ctx = new ThreadLocal<>();
    private STCLifecycleExecutor lifecycleExecutor = new STCLifecycleExecutor(this);

    // Generic

    Stage stage;
    final ReentrantReadWriteLock executionLock = new ReentrantReadWriteLock();
    List<File> tempFiles = new ArrayList<>();
    List<Class<? extends Exception>> fatalExceptions;
    boolean executing;
    ThreadLocal<List<String>> importPaths = new ThreadLocal<>();
    ResourceManager resourceManager;
    ServiceManager serviceManager;
    ProvidersManagementService providersManagementService = new ProvidersManagementService();

    // Libraries

    final ReentrantReadWriteLock libraryLock = new ReentrantReadWriteLock();
    boolean newLibAdded;
    Reflections reflections;
    List<Library> libraries = new ArrayList<>();
    LibraryClassLoader libraryClassloader = new LibraryClassLoader(new URL[0], STContext.class.getClassLoader());

    // Resources

    final ReadWriteLock rootResourceLock = new ReentrantReadWriteLock();
    ReentrantReadWriteLock resourceListLock = new ReentrantReadWriteLock();
    List<Resource> resources = new ArrayList<>();
    final ThreadLocal<Resource> resourceScope = new ThreadLocal<>();
    final Map<Resource, List<Resource>> parentToPendingChildrenMap = new HashMap<>();
    final HashSet<Resource> postChildrenExecuted = new HashSet<>();
    Resource defaultParent;
    List<ResourceDefinition> resourceDefinitions = new ArrayList<>();
    HashMap<String, Resource> resourcesUidIndex = new HashMap<>();
    HashMap<Resource, List<Resource>> parentChildIndex;
    /**
     * Flag indicating if element creation is allowed
     */
    boolean createAllowed = true;
    final Map<FQName, ResourceDefinition> resourceDefinitionsFQNIndex = new HashMap<>();
    HashSet<FQName> uniqueResourcesCreated = new HashSet<>();
    HashSet<ManyToManyResourceDependency> m2mDependencies = new HashSet<>();
    HashSet<OneToManyResourceDependency> o2mDependencies = new HashSet<>();
    final ThreadLocal<String> sourceUrl = new ThreadLocal<>();

    // Hosts

    Host host;

    // Scripting

    ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    final HashMap<String, ScriptEngine> scriptEnginesByExtCache = new HashMap<>();

    // Notifications

    List<AutoNotify> autoNotifications = new ArrayList<>();
    ListHashMap<Resource, AutoNotify> autoNotificationsSourceIndex = new ListHashMap<>();
    ListHashMap<Resource, AutoNotify> autoNotificationsTargetIndex = new ListHashMap<>();
    final LinkedList<Notification> notificationsPending = new LinkedList<>();

    static {
        try {
            scriptingSupport.put("rb", IOUtils.toString(STContext.class.getResourceAsStream("ruby/systyrant.rb")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public STContext() throws InvalidResourceDefinitionException, STRuntimeException {
        this(new LocalHost());
    }

    public STContext(Host host) throws InvalidResourceDefinitionException, STRuntimeException {
        this.host = host;
        scriptEngineManager.registerEngineExtension("stl", new DSLScriptingEngineFactory(this));
        resourceManager = new ResourceManagerImpl(this);
        serviceManager = new ServiceManagerImpl(this);
        registerLibrary(new Library());
        providersManagementService.init(reflections);
        inject(host);
    }

    // ------------------------------------------------------------------------------------------
    // Libraries
    // ------------------------------------------------------------------------------------------

    public void registerLibraries(File libDir) {
        logger.debug("Scanning libraries directory {}", libDir);
        if (libDir.exists()) {
            File[] childrens = libDir.listFiles();
            if (childrens != null) {
                for (File children : childrens) {
                    try {
                        registerLibrary(children);
                    } catch (IOException e) {
                        logger.error("Failed to load module {}: {}", children.getPath(), e.getLocalizedMessage());
                    } catch (InvalidResourceDefinitionException e) {
                        logger.error("Invalid resource definition: {}", e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    public void registerLibrary(File libraryFile) throws IOException, InvalidResourceDefinitionException {
        logger.debug("Registering module {}", libraryFile.getPath());
        Library library = new Library(libraryFile);
        registerLibrary(library);
    }

    private void registerLibrary(Library library) throws InvalidResourceDefinitionException {
        libraryLock.writeLock().lock();
        try {
            newLibAdded = true;
            libraries.add(library);
            logger.debug("Adding library {} to the module classloader", library.getLocationUrl());
            libraryClassloader.addURL(library.getLocationUrl());
            for (Class<?> clazz : library.getResourceDefinitionClasses()) {
                resourceManager.registerJavaResource(clazz);
            }
            Set<Class<?>> services = library.getReflections().getTypesAnnotatedWith(Service.class);
            try {
                for (Class<?> service : services) {
                    serviceManager.registerService(service);
                }
            } catch (InvalidServiceException e) {
                throw new InvalidResourceDefinitionException(e.getMessage(), e);
            }
            if (reflections != null) {
                reflections.merge(library.getReflections());
            } else {
                reflections = library.getReflections();
            }
        } finally {
            libraryLock.writeLock().unlock();
        }
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public ProvidersManagementService getProvidersManagementService() {
        return providersManagementService;
    }

    public List<Library> getLibraries() {
        return Collections.unmodifiableList(libraries);
    }

    // ------------------------------------------------------------------------------------------
    // Resource setup / registration
    // ------------------------------------------------------------------------------------------

    public void runScriptFile(String path) throws IOException, ScriptException {
        runScriptFile(null, path);
    }

    public void runScriptFile(@NotNull URL url) throws IOException, ScriptException {
        runScriptFile(null, url);
    }

    public void runScriptFile(String pkg, @NotNull URL url) throws IOException, ScriptException {
        try {
            runScriptFile(pkg, url.toURI());
        } catch (URISyntaxException e) {
            throw new ScriptException("Invalid url: " + url);
        }
    }

    public synchronized void runScriptFile(@NotNull URI uri) throws IOException, ScriptException {
        runScriptFile(null, uri);
    }

    public synchronized void runScriptFile(String pkg, String path) throws IOException, ScriptException {
        URL script = getClass().getResource(path);
        if (script == null) {
            script = ClassLoader.getSystemResource(path);
        }
        if (script == null) {
            throw new IOException("Unable to find script " + path);
        }
        try {
            runScriptFile(pkg, script.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid path: " + path);
        }
    }

    public synchronized void runScriptFile(String pkg, @NotNull URI uri) throws IOException, ScriptException {
        if (!uri.isAbsolute()) {
            uri = new File(uri.getPath()).getAbsoluteFile().toURI();
        }
        URL url = uri.toURL();
        try (InputStreamReader reader = new InputStreamReader(url.openConnection().getInputStream())) {
            runScriptFile(pkg, uri.toString(), reader);
        }
    }

    public synchronized void runScript(String uri, Reader scriptReader) throws IOException, ScriptException {
        runScriptFile(null, uri, scriptReader);
    }

    public void runScript(String dsl) throws IOException, ScriptException {
        runScript(dsl, "stl");
    }

    /**
     * Run a script.
     *
     * @param script Script to run
     * @param ext    Extension of the script
     * @throws IOException     If an error occured while reading the script.
     * @throws ScriptException If there is an error in the script.
     */
    public void runScript(String script, String ext) throws IOException, IllegalArgumentException, ScriptException {
        runScriptFile(null, "dynamic:" + UUID.randomUUID().toString() + "." + ext, new StringReader(script));
    }

    public synchronized void runScriptFile(String pkg, String uri, Reader scriptReader) throws IOException, ScriptException {
        String oldSource = sourceUrl.get();
        sourceUrl.set(uri);
        try {
            if (uri == null) {
                uri = UUID.randomUUID().toString() + ".stl";
            }
            int dotIdx = uri.lastIndexOf('.');
            if (dotIdx == -1) {
                throw new IOException("path has no extension: " + uri);
            }
            String ext = uri.substring(dotIdx + 1).toLowerCase();
            ScriptEngine scriptEngine = getScriptEngineByExt(ext);
            boolean ctxMissing = ctx.get() == null;
            if (ctxMissing) {
                ctx.set(this);
            }
            Bindings bindings = scriptEngine.getBindings(ENGINE_SCOPE);
            bindings.put("package", pkg);
            bindings.put("ctx", this);
            bindings.put("stsm", getServiceManager());
            bindings.put("strm", getResourceManager());
            String support = scriptingSupport.get(ext);
            if (support != null) {
                scriptEngine.eval(support, bindings);
            }
            scriptEngine.eval(scriptReader, bindings);
            if (ctxMissing) {
                ctx.remove();
            }
        } finally {
            if (oldSource != null) {
                sourceUrl.set(oldSource);
            } else {
                sourceUrl.remove();
            }
        }
    }

    /**
     * Get a cached script engine. Please note that since we're caching them, those are not thread-safe due to the requirement
     * of passing the package as a binding.
     *
     * @param ext Extension
     * @return Script Engine
     * @throws IOException
     */
    private ScriptEngine getScriptEngineByExt(String ext) throws IOException {
        ScriptEngine scriptEngine;
        synchronized (scriptEnginesByExtCache) {
            scriptEngine = scriptEnginesByExtCache.get(ext);
            if (scriptEngine == null) {
                scriptEngine = scriptEngineManager.getEngineByExtension(ext);
                if (scriptEngine == null) {
                    throw new IOException("Unable to run scripts with the extension: " + ext);
                }
                scriptEnginesByExtCache.put(ext, scriptEngine);
            }
        }
        return scriptEngine;
    }

    public synchronized Stage getStage() {
        return stage;
    }

    public Host host() {
        return host;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) throws STRuntimeException {
        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null");
        }
        executionLock.writeLock().lock();
        if (stage != null && stage.ordinal() >= Stage.EXECUTE.ordinal()) {
            throw new STRuntimeException("The context host can only be changed before the execution stage");
        }
        try {
            this.host = host;
            inject(host);
        } finally {
            executionLock.writeLock().unlock();
        }
    }

    public FileStore files() throws InvalidServiceException {
        return serviceManager.getService(FileStore.class);
    }

    public String getSourceUrl() {
        return sourceUrl.get();
    }

    public void setSourceUrl(String url) {
        sourceUrl.set(url);
    }

    public void clearSourceUrl() {
        sourceUrl.remove();
    }

    // ------------------------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------------------------

    public boolean execute() throws STRuntimeException {
        STContext.ctx.set(this);
        try {
            host.start();
            return lifecycleExecutor.execute();
        } finally {
            host.stop();
            STContext.ctx.remove();
        }
    }


    @Override
    public void close() {
        ctx.remove();
        resourceManager.close();
        for (Library library : libraries) {
            library.close();
        }
        serviceManager.close();
    }

    // ------------------------------------------------------------------------------------------
    // Notification
    // ------------------------------------------------------------------------------------------

    public void notify(String resourceQuery, String category) throws InvalidQueryException {
        for (Resource resource : findResources(resourceQuery)) {
            notify(resource, category);
        }
    }

    public void notify(Resource resource) {
        notify(currentResource(), resource, null);
    }

    public void notify(Resource resource, String category) {
        notify(currentResource(), resource, category);
    }

    public void notify(Resource source, Resource resource, String category) {
        synchronized (notificationsPending) {
            notificationsPending.add(new Notification(category, source, resource));
        }
    }

    public void addAutoNotification(AutoNotify autoNotify) {
        add(autoNotify);
    }

    // ------------------------------------------------------------------------------------------
    // Manage scopes
    // ------------------------------------------------------------------------------------------

    public Resource currentResource() {
        return resourceScope.get();
    }

    public void setCurrentResource(Resource resource) {
        resourceScope.set(resource);
    }

    public List<String> getImports() {
        List<String> list = importPaths.get();
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

    public void addImport(String value) {
        List<String> list = importPaths.get();
        if (list == null) {
            list = new ArrayList<>();
            importPaths.set(list);
        }
        list.add(value);
    }

    public void clearImports() {
        importPaths.remove();
    }

    // ------------------------------------------------------------------------------------------
    // Other runtime methods for use by resources or calling code
    // ------------------------------------------------------------------------------------------

    public static STContext get() {
        return ctx.get();
    }

    public boolean hasResources() {
        return resourceManager.hasResources();
    }

    // ------------------------------------------------------------------------------------------
    // Resources lookup
    // ------------------------------------------------------------------------------------------

    public List<Resource> getResources() {
        return resourceManager.getResources();
    }

    /**
     * Find a resource using the current resource's parent as a base resource.
     *
     * @param query Query.
     * @return List of resources that match the specified query.
     * @throws InvalidQueryException If the query was invalid.
     * @see #currentResource()
     */
    @NotNull
    public List<Resource> findResources(String query) throws InvalidQueryException {
        Resource baseResource = currentResource();
        if (baseResource != null) {
            baseResource = baseResource.getParent();
        }
        return resourceManager.findResources(query, baseResource);
    }

    @NotNull
    public List<Resource> findResources(String query, Resource baseResource) throws InvalidQueryException {
        return resourceManager.findResources(query, baseResource);
    }

    public Resource findResourceByUid(String uid) {
        return resourceManager.findResourcesByUid(uid);
    }

    // ------------------------------------------------------------------------------------------
    // Other
    // ------------------------------------------------------------------------------------------

    /**
     * Retrieve the root resource lock.
     * This is currently used to make id generation threadsafe for resources with no parents.
     *
     * @return ReadWriteLock
     */
    public ReadWriteLock getRootResourceLock() {
        return rootResourceLock;
    }

    public ClassLoader getLibraryClassloader() {
        if (libraryClassloader != null) {
            return libraryClassloader;
        } else {
            return getClass().getClassLoader();
        }
    }

    public void registerTempFile(File tempFile) {
        tempFiles.add(tempFile);
    }

    public synchronized Resource getDefaultParent() {
        return defaultParent;
    }

    public synchronized void setDefaultParent(Resource defaultParent) {
        this.defaultParent = defaultParent;
    }

    public List<Class<? extends Exception>> getFatalExceptions() {
        return fatalExceptions;
    }

    public void setFatalExceptions(List<Class<? extends Exception>> fatalExceptions) {
        this.fatalExceptions = fatalExceptions;
    }

    @SafeVarargs
    public final void setFatalExceptions(Class<? extends Exception>... fatalExceptions) {
        this.fatalExceptions = fatalExceptions != null ? Arrays.asList(fatalExceptions) : null;
    }

    public void clearFatalException() {
        fatalExceptions = null;
    }

    public Object invokeMethod(String name, Parameters params) throws STRuntimeException {
        return serviceManager.invokeMethod(name, params);
    }

    public void inject(Object obj) throws InjectException {
        Class<?> cl = obj.getClass();
        while (cl != null) {
            for (Field field : cl.getDeclaredFields()) {
                Provider provider = field.getAnnotation(Provider.class);
                Class<?> type = field.getType();
                try {
                    if (provider != null) {
                        ProviderManager pm = providersManagementService.getProviderManager(type.asSubclass(ProviderManager.class));
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        field.set(obj, pm);
                    }
                    Inject inject = field.getAnnotation(Inject.class);
                    if (inject != null) {
                        if (STContext.class.isAssignableFrom(type)) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            field.set(obj, this);
                        } else {
                            throw new InjectException("Cannot inject object " + obj.getClass().getName() + "#" + field.getName());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new InjectException("Cannot inject object " + obj.getClass().getName() + "#" + field.getName());
                }
            }
            cl = cl.getSuperclass();
        }
    }


    List<Resource> getChildrensInternalList(Resource resource) {
        List<Resource> childrens = parentChildIndex.get(resource);
        if (childrens == null) {
            childrens = new ArrayList<>();
            parentChildIndex.put(resource, childrens);
        }
        return childrens;
    }

    Resource getUnpreparedResource() {
        for (Resource resource : resources) {
            if (resource.isExecutable() && !resource.isFailed() && resource.getStage().ordinal() < Stage.PREPARE.ordinal()) {
                return resource;
            }
        }
        return null;
    }

    void setResourceScope(Resource resource) {
        resourceScope.set(resource);
        MDC.put("resource", resource.toString());
    }

    void clearResourceScope() {
        resourceScope.remove();
        MDC.remove("resource");
    }

    List<AutoNotify> findAutoNotificationBySource(Resource source) {
        return autoNotificationsSourceIndex.get(source);
    }

    List<AutoNotify> findAutoNotificationByTarget(Resource target) {
        return autoNotificationsTargetIndex.get(target);
    }

    Notification popPendingNotifications() {
        synchronized (notificationsPending) {
            if (notificationsPending.isEmpty()) {
                return null;
            } else {
                return notificationsPending.removeFirst();
            }
        }
    }

    void add(AutoNotify autoNotify) {
        synchronized (autoNotifications) {
            autoNotifications.add(autoNotify);
            for (Resource source : autoNotify.getSources()) {
                autoNotificationsSourceIndex.get(source).add(autoNotify);
            }
            autoNotificationsTargetIndex.get(autoNotify.getTarget()).add(autoNotify);
        }
    }

    void remove(AutoNotify autoNotify) {
        synchronized (autoNotifications) {
            autoNotifications.remove(autoNotify);
            for (Resource source : autoNotify.getSources()) {
                autoNotificationsSourceIndex.get(source).remove(autoNotify);
            }
            for (ArrayList<AutoNotify> sourceIdxList : autoNotificationsSourceIndex.values()) {
                sourceIdxList.remove(autoNotify);
            }
            for (ArrayList<AutoNotify> targetIdxList : autoNotificationsTargetIndex.values()) {
                targetIdxList.remove(autoNotify);
            }
        }
    }

    void mergeNotification(AutoNotify autonotify, AutoNotify toMerge) {
        synchronized (autoNotifications) {
            remove(toMerge);
            for (Resource source : toMerge.getSources()) {
                autoNotificationsSourceIndex.get(source).add(autonotify);
            }
        }
    }
}

