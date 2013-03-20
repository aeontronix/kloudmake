/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import com.kloudtek.systyrant.annotation.Provider;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.systyrant.dsl.DSLScriptingEngineFactory;
import com.kloudtek.systyrant.exception.*;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.host.LocalHost;
import com.kloudtek.systyrant.provider.ProviderManager;
import com.kloudtek.systyrant.provider.ProvidersManagementService;
import com.kloudtek.systyrant.resource.JavaResourceFactory;
import com.kloudtek.systyrant.resource.Resource;
import com.kloudtek.systyrant.resource.ResourceManager;
import com.kloudtek.systyrant.resource.ResourceManagerImpl;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.ServiceManagerImpl;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.util.AutoCreateHashMap;
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
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.kloudtek.systyrant.Stage.EXECUTE;
import static com.kloudtek.systyrant.resource.Resource.State.*;
import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;
import static javax.script.ScriptContext.ENGINE_SCOPE;

/**
 * This is the "brains" of SysTyrant, which contains the application's state
 */
public class STContext implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(STContext.class);
    private static List<Resource.State> failurePropagationStates = Arrays.asList(NEW, PREPARED);
    private ResourceManager resourceManager;
    private ServiceManager serviceManager;
    private List<Library> libraries = new ArrayList<>();
    private LibraryClassLoader libraryClassloader = new LibraryClassLoader(new URL[0], STContext.class.getClassLoader());
    private Reflections reflections;
    private static ThreadLocal<STContext> ctx = new ThreadLocal<>();
    private ThreadLocal<Resource> resourceScope = new ThreadLocal<>();
    private final Map<String, Resource> resourceUidIndexes = new HashMap<>();
    private final Map<String, Resource> resourceIdIndexes = new HashMap<>();
    private final Map<Resource, List<Resource>> parentToPendingChildrenMap = new HashMap<>();
    private final HashSet<Resource> postChildrenExecuted = new HashSet<>();
    private boolean executed;
    private List<File> tempFiles = new ArrayList<>();
    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private final HashMap<String, ScriptEngine> scriptEnginesByExtCache = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Resource defaultParent;
    private boolean newLibAdded;
    private ProvidersManagementService providersManagementService = new ProvidersManagementService();
    private List<Class<? extends Exception>> fatalExceptions;
    private Host host;
    private boolean executing;

    public STContext() throws InvalidResourceDefinitionException, STRuntimeException, InjectException {
        this(new LocalHost());
        host.start();
    }

    public STContext(Host host) throws InjectException, InvalidResourceDefinitionException, STRuntimeException {
        this.host = host;
        scriptEngineManager.registerEngineExtension("stl", new DSLScriptingEngineFactory(this));
        resourceManager = new ResourceManagerImpl(this);
        serviceManager = new ServiceManagerImpl(this, reflections);
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
        lock.writeLock().lock();
        try {
            newLibAdded = true;
            libraries.add(library);
            logger.debug("Adding library {} to the module classloader", library.getLocationUrl());
            libraryClassloader.addURL(library.getLocationUrl());
            for (JavaResourceFactory javaResourceFactory : library.getJavaElFactories()) {
                resourceManager.registerResources(javaResourceFactory);
            }
            Set<Class<?>> services = library.getReflections().getTypesAnnotatedWith(Service.class);
            try {
                for (Class<?> service : services) {
                    serviceManager.registerService(service);
                }
            } catch (InvalidServiceException e) {
                throw new InvalidResourceDefinitionException(e.getMessage(), e);
            }
            if( reflections != null ) {
                reflections.merge(library.getReflections());
            } else {
                reflections = library.getReflections();
            }
        } finally {
            lock.writeLock().unlock();
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

    public void runScript(String path) throws IOException, ScriptException {
        runScript(null, path);
    }

    public synchronized void runScript(String pkg, String path) throws IOException, ScriptException {
        URL script = getClass().getResource(path);
        if (script == null) {
            script = ClassLoader.getSystemResource(path);
        }
        if (script == null) {
            throw new IOException("Unable to find script " + path);
        }
        try {
            runScript(pkg, script.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid path: " + path);
        }
    }

    public void runScript(@NotNull URL url) throws IOException, ScriptException {
        runScript(null, url);
    }

    public void runScript(String pkg, @NotNull URL url) throws IOException, ScriptException {
        try {
            runScript(pkg, url.toURI());
        } catch (URISyntaxException e) {
            throw new ScriptException("Invalid url: " + url);
        }
    }

    public synchronized void runScript(@NotNull URI uri) throws IOException, ScriptException {
        runScript(null, uri);
    }

    public synchronized void runScript(String pkg, @NotNull URI uri) throws IOException, ScriptException {
        if( ! uri.isAbsolute() ) {
            uri = new File(uri.getPath()).getAbsoluteFile().toURI();
        }
        InputStreamReader reader = new InputStreamReader(uri.toURL().openConnection().getInputStream());
        runScript(pkg, uri.toString(), reader);
        reader.close();
    }

    public synchronized void runScript(String path, Reader scriptReader) throws IOException, ScriptException {
        runScript(null, path, scriptReader);
    }

    public synchronized void runScript(String pkg, String path, Reader scriptReader) throws IOException, ScriptException {
        if (path == null) {
            path = UUID.randomUUID().toString() + ".stl";
        }
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx == -1) {
            throw new IOException("path has no extension: " + path);
        }
        String ext = path.substring(dotIdx + 1);
        ScriptEngine scriptEngine = getScriptEngineByExt(ext);
        Bindings bindings = scriptEngine.getBindings(ENGINE_SCOPE);
        bindings.put("package", pkg);
        bindings.put("stctx", this);
        bindings.put("stsm", getServiceManager());
        bindings.put("strm", getResourceManager());
        scriptEngine.eval(scriptReader, bindings);
    }

    public void runDSLScript(String dsl) throws IOException, ScriptException {
        runScript(null, new StringReader(dsl));
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

    public Host host() throws InvalidServiceException {
        return host;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) throws STRuntimeException {
        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null");
        }
        lock.writeLock().lock();
        try {
            this.host = host;
            inject(host);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public FileStore files() throws InvalidServiceException {
        return serviceManager.getService(FileStore.class);
    }

    // ------------------------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------------------------

    @Override
    public void close() {
        ctx.remove();
        resourceManager.close();
        for (Library library : libraries) {
            library.close();
        }
        serviceManager.close();
    }

    public boolean execute() throws STRuntimeException {
        lock.writeLock().lock();
        try {
            executing = true;

            logger.info("Systyrant context execution started");
            ctx.set(this);

            try {
                boolean successful;

                prepare();

                resourceManager.prepareForExecution();

                validateAndGenerateIdentifiers();

                buildIndexes();

                executeResources(EXECUTE);

                cleanup();

                successful = isSuccessful();

                if (successful) {
                    logger.info("Systyrant context execution completed");
                } else {
                    logger.warn("Systyrant context execution completed with some errors");
                }
                executed = true;
                return successful;
            } finally {
                for (File tempFile : tempFiles) {
                    if (!tempFile.delete()) {
                        logger.warn("Failed to delete temporary file " + tempFile.getPath() + " will attempt to delete on process exit");
                        tempFile.deleteOnExit();
                    }
                }
                resourceManager.setCreateAllowed(true);
                ctx.remove();
            }
        } finally {
            executing = false;
            lock.writeLock().unlock();
        }
    }

    private void executeResources(Stage stage) throws STRuntimeException {
        Map<Resource, List<Resource>> parentchildrens = new HashMap<>();
        for (Map.Entry<Resource, List<Resource>> entry : parentToPendingChildrenMap.entrySet()) {
            parentchildrens.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Resource resource : resourceManager) {
            resourceScope.set(resource);
            executeResourceActions(resource, stage, false);
            resourceScope.remove();
            for (Resource parent = resource.getParent(), child = resource; parent != null; parent = parent.getParent()) {
                List<Resource> childsofchild = parentchildrens.get(child);
                List<Resource> siblings = parentchildrens.get(parent);
                if (childsofchild == null || childsofchild.isEmpty()) {
                    siblings.remove(child);
                    if (siblings.isEmpty()) {
                        resourceScope.set(parent);
                        executeResourceActions(parent, stage, true);
                        resourceScope.remove();
                    }
                }
                child = parent;
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
        for (Resource resource : resourceManager) {
            Resource parent = resource.getParent();
            if (parent != null) {
                List<Resource> childrens = parentToPendingChildrenMap.get(parent);
                if (childrens == null) {
                    childrens = new LinkedList<>();
                    parentToPendingChildrenMap.put(parent, childrens);
                }
                childrens.add(resource);
            }
        }
    }

    private Resource getUnpreparedResource() {
        for (Resource resource : resourceManager) {
            if (resource.getState().ordinal() < PREPARED.ordinal()) {
                return resource;
            }
        }
        return null;
    }

    private void prepare() throws STRuntimeException {
        host.start();
        if (newLibAdded) {
            providersManagementService.init(reflections);
        }
        for (Resource resource : resourceManager) {
            resource.reset();
        }
        serviceManager.start();
        for (Resource res = getUnpreparedResource(); res != null; res = getUnpreparedResource()) {
            resourceScope.set(res);
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
            resourceScope.remove();
        }
        resourceManager.resolveDependencies(false);
        resourceManager.setCreateAllowed(false);
    }

    private void fatalFatalException(Throwable e) throws STRuntimeException {
        if (e instanceof STRuntimeException && e.getCause() != null) {
            e = e.getCause();
        }
        if (fatalExceptions != null && !fatalExceptions.isEmpty()) {
            for (Class<? extends Exception> fatalException : fatalExceptions) {
                if (fatalException.isAssignableFrom(e.getClass())) {
                    throw new STRuntimeException("Fatal exception caught: " + e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void cleanup() {
        for (Resource resource : resourceManager) {
            setResourceScope(resource);
            try {
                resource.executeActions(Stage.CLEANUP, false);
            } catch (STRuntimeException e) {
                logger.warn("Error occured during cleanup: " + e.getMessage(), e);
            }
            clearResourceScope();
        }
        serviceManager.stop();
        host.stop();
    }

    /**
     * This will generate Uids for resources that don't have one.
     * The logic this follows is to use the id if one has been defined and it hasn't been used already, or to use the
     * resource value plus a counter (ie: file1, file2, etc)
     */
    public synchronized void validateAndGenerateIdentifiers() throws InvalidAttributeException {
        resourceUidIndexes.clear();

        HashSet<String> uids = new HashSet<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        AutoCreateHashMap<Resource, HashSet<String>> parentToChildIdsMap = new AutoCreateHashMap<Resource, HashSet<String>>() {
            @Override
            protected HashSet<String> create() {
                return new HashSet<>();
            }
        };

        for (Resource resource : resourceManager) {
            String id = resource.getId();
            String uid = resource.getUid();
            FQName fqName = resource.getType();
            HashSet<String> idsForParent = parentToChildIdsMap.get(resource.getParent());
            if (isEmpty(id)) {
                int i = 1;
                if (isNotEmpty(uid) && !idsForParent.contains(uid)) {
                    id = uid;
                } else {
                    while (id == null) {
                        String proposedId = fqName.toString() + i++;
                        if (!idsForParent.contains(proposedId)) {
                            id = proposedId;
                        }
                    }
                }
                resource.setId(id);
            }
            if (idsForParent.contains(id)) {
                throw new InvalidAttributeException("Duplicated id found for " + fqName.toString() + ": " + id);
            } else {
                idsForParent.add(id);
            }
            if (isEmpty(uid)) {
                uid = resource.generateUid();
                resource.setUid(uid);
            }
            if (uids.contains(uid)) {
                throw new InvalidAttributeException("Duplicated uid found for " + fqName.toString() + ": " + uid);
            } else {
                uids.add(uid);
            }
        }
    }

    private void handleResourceFailure(Resource resource) {
        LinkedList<Resource> list = new LinkedList<>();
        list.add(resource);
        while (!list.isEmpty()) {
            Resource el = list.removeFirst();
            el.setState(FAILED);
            for (Resource dep : getDependentOn(el)) {
                if (failurePropagationStates.contains(dep.getState())) {
                    list.addLast(dep);
                }
            }
        }
    }

    @NotNull
    private Collection<? extends Resource> getDependentOn(Resource el) {
        ArrayList<Resource> list = new ArrayList<>();
        for (Resource resource : resourceManager) {
            if (resource.getDependencies().contains(el)) {
                list.add(resource);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------------------------------
    // Manage scopes
    // ------------------------------------------------------------------------------------------

    public Resource currentResource() {
        return resourceScope.get();
    }

    private void setResourceScope(Resource resource) {
        resourceScope.set(resource);
        MDC.put("resource", resource.toString());
    }

    private void clearResourceScope() {
        resourceScope.remove();
        MDC.remove("resource");
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

    public synchronized boolean isSuccessful() {
        for (Resource resource : resourceManager) {
            if (resource.getState() == FAILED) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------
    // Resources lookup
    // ------------------------------------------------------------------------------------------

    public List<Resource> getResources() {
        return resourceManager.getResources();
    }

    @NotNull
    public List<Resource> findResources(String query) throws InvalidQueryException {
        return resourceManager.findResources(query,currentResource());
    }

    @NotNull
    public List<Resource> findResources(String query, Resource baseResource) throws InvalidQueryException {
        return resourceManager.findResources(query,baseResource);
    }

    public Resource findResourceByUid(String uid) {
        for (Resource resource : resourceManager) {
            if (uid.equals(resource.getUid())) {
                return resource;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------
    // Other
    // ------------------------------------------------------------------------------------------

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

    public void inject(Object obj) throws InjectException {
        Class<?> cl = obj.getClass();
        while (cl != null) {
            for (Field field : cl.getDeclaredFields()) {
                Provider provider = field.getAnnotation(Provider.class);
                if (provider != null) {
                    ProviderManager pm = providersManagementService.getProviderManager(field.getType().asSubclass(ProviderManager.class));
                    field.setAccessible(true);
                    try {
                        field.set(obj, pm);
                    } catch (IllegalAccessException e) {
                        throw new InjectException("Cannot inject object " + obj.getClass().getName() + "#" + field.getName());
                    }
                }
            }
            cl = cl.getSuperclass();
        }
    }


    public class LibraryClassLoader extends URLClassLoader {
        public LibraryClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public LibraryClassLoader(URL[] urls) {
            super(urls);
        }

        public LibraryClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }

}

