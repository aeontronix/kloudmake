/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import com.aeontronix.aeonbuild.annotation.KMResource;
import com.aeontronix.aeonbuild.exception.InvalidResourceDefinitionException;
import com.aeontronix.aeonbuild.resource.core.FileResource;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.xml.XPathUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Library {
    private static final Logger logger = LoggerFactory.getLogger(Library.class);
    private File localLocation;
    private URI localLocationUri;
    private URI locationUri;
    private URL locationUrl;
    private ClassLoader classLoader;
    private ZipFile zipFile;
    private Reflections reflections;
    private List<Class<?>> resourceDefinitionClasses = new ArrayList<>();
    private HashMap<String, String> stPkgToJavaPkgMap = new HashMap<>();
    private HashMap<String, String> javaPkgToStPkgMap = new HashMap<>();
    private static Reflections classpathReflections;

    public Library() throws InvalidResourceDefinitionException {
        classLoader = getClass().getClassLoader();
        scan();
    }

    public Library(File localLocation) throws IOException, InvalidResourceDefinitionException {
        this.localLocation = localLocation;
        if (localLocation.isDirectory()) {
            localLocationUri = localLocation.toURI();
        } else {
            localLocationUri = URI.create("jar:" + localLocation.toURI().toString() + "!/");
            zipFile = new ZipFile(localLocation);
        }
        locationUri = localLocation.toURI();
        locationUrl = locationUri.toURL();
        classLoader = new URLClassLoader(new URL[]{locationUrl});
        scan();
    }

    private void scan() throws InvalidResourceDefinitionException {
        // TODO support pre-generation of reflection data
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        if (locationUrl != null) {
            reflections = new Reflections(configurationBuilder.setUrls(locationUrl).addClassLoader(classLoader));
        } else {
            synchronized (Library.class) {
                if (classpathReflections == null) {
                    List<String> pkgs = new ArrayList<>();
                    try {
                        Enumeration<URL> descriptors = getClass().getClassLoader().getResources("META-INF/aeonbuild.xml");
                        while (descriptors.hasMoreElements()) {
                            try (InputStream stream = descriptors.nextElement().openStream()) {
                                pkgs.addAll(XPathUtils.evalXPathTextElements("kloudmake/pkg/text()", new InputSource(stream)));
                            }
                        }
                    } catch (IOException | XPathExpressionException e) {
                        throw new InvalidResourceDefinitionException(e);
                    }
                    Set<URL> urls = new HashSet<>();
                    for (String pkg : pkgs) {
                        urls.addAll(ClasspathHelper.forPackage(pkg));
                    }
                    classpathReflections = new Reflections(configurationBuilder.setUrls(urls));
                }
            }
            reflections = classpathReflections;
        }
        Set<Class<?>> javaElements = reflections.getTypesAnnotatedWith(KMResource.class);
        for (Class<?> clazz : javaElements) {
            if (clazz.getSimpleName().equals("package-info")) {
                KMResource annotation = clazz.getAnnotation(KMResource.class);
                assert annotation != null;
                String pkg = annotation.value();
                if (StringUtils.isNotEmpty(pkg)) {
                    String spkg = pkg.toLowerCase();
                    String jpkg = clazz.getPackage().getName();
                    stPkgToJavaPkgMap.put(spkg, jpkg);
                    javaPkgToStPkgMap.put(jpkg, spkg);
                }
            }
        }
        for (Class<?> clazz : javaElements) {
            if (!clazz.getSimpleName().equals("package-info")) {
                resourceDefinitionClasses.add(clazz);
            }
        }
    }

    public Library(File local, URI uri) throws IOException, InvalidResourceDefinitionException {
        this(local);
        this.locationUri = uri;
    }

    public void close() {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                //
            }
        }
    }

    public File getLocalLocation() {
        return localLocation;
    }

    public URI getLocalLocationUri() {
        return localLocationUri;
    }

    public URI getLocationUri() {
        return locationUri;
    }

    public Reflections getReflections() {
        return reflections;
    }

    public URL getLocationUrl() {
        return locationUrl;
    }

    public List<Class<?>> getResourceDefinitionClasses() {
        return resourceDefinitionClasses;
    }

    public URI getResource(String path) {
        if (localLocation.isDirectory()) {
            File file = new File(localLocation + File.separator + path.replace("/", File.separator));
            if (file.exists()) {
                return file.toURI();
            }
        } else {
            ZipEntry entry = zipFile.getEntry(path);
            if (entry != null) {
                return URI.create(localLocationUri + path);
            }
        }
        return null;
    }

    public URL getElementScript(String pkg, String name) {
        String mapping = stPkgToJavaPkgMap.get(pkg);
        if (mapping != null) {
            pkg = mapping;
        }
        String path = pkg.replace(".", "/") + "/" + name + ".stl";
        // TODO use reflections as index
        URL resource = classLoader.getResource(path);
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to get resource {} in library {}", path, localLocation);
        }
        return resource;
    }

    private static String getPkgName(Class<FileResource> fileResourceClass) {
        return fileResourceClass.getPackage().getName();
    }

    private String builtin(String pkg) {
        return "com.aeontronix.aeonbuild.resource.builtin." + pkg;
    }
}
