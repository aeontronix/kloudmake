/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.filestore;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Default;
import com.kloudtek.systyrant.annotation.Method;
import com.kloudtek.systyrant.annotation.Param;
import com.kloudtek.systyrant.annotation.Service;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.TempFile;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.kloudtek.util.StringUtils.*;

/**
 * <p>This service allows to access local or remote files.</p>
 * <p>It supports remote files, which are subsequently cached on the host. It also support remote files which cannot be
 * automatically downloaded, but need to instead be manually downloaded by the user (generally because of license
 * restrictions on distribution).</p>
 * <p>Local files will not be cached (the ones where protocol is 'classpath' or 'file')</p>
 * <p>The URI can be either any of the standard java-supported URL, or one of the following schemes:</p>
 * <dl>
 * <dt>classpath</dt>
 * <dd>Used to retrieve a file in the classpath. ie: classpath:/com/test/file.txt</dd>
 * </dl>
 */
@Service
public class FileStore implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(FileStore.class);
    private LinkedHashSet<String> locations = new LinkedHashSet<>();
    private HttpClient httpClient = new HttpClient();
    private List<TempFile> temporaryFiles = new LinkedList<>();

    public static void main(String[] args) {
        new FileStore();
    }

    public FileStore() {
        locations.add(System.getProperty("user.home") + File.separator + ".systyrant" + File.separator + "files");
        locations.add("files");
    }

    @Override
    public synchronized void close() {
        for (TempFile file : temporaryFiles) {
            try {
                file.close();
            } catch (IOException e) {
                logger.warn("Error deleting temporary file " + file.getAbsolutePath(), e);
            }
        }
    }

    public synchronized DataFile create(@NotNull String uri) throws IOException, TemplateException {
        URI u = URI.create(uri);
        if (isEmpty(u.getScheme())) {
            throw new IllegalArgumentException("Invalid uri " + uri);
        }
        if (u.getScheme().equals("libfile")) {
            return new LocalDataFile(u.getSchemeSpecificPart());
        } else {
            return null;
        }
    }

    public synchronized Collection<String> getLocations() {
        return Collections.unmodifiableSet(locations);
    }

    public synchronized void addLocation(String location) {
        locations.add(location);
    }

    public synchronized void removeLocation(String location) {
        locations.remove(location);
    }

    /**
     * <p>This function is used to generate an url to a file contained in a library. This is generally used to generate
     * an url to be passed as a 'source' attribute to a 'core:file' resource.</p>
     * <p>Relative paths will be relative to the source of the resource being processed. For example if you have the
     * following two files in the same directory: 'tomcat6.stl' and 'tomcat6-server.xml.ftl', and you wanted to refer
     * generate an url to the 'tomcat6-server.xml.ftl' from the stl file, you could do the following:</p>
     * <code>
     * def tomcat6( serverport=8005, httpport = 8080, serverxml='/etc/tomcat6/server.xml' ) {<br/>
     * new core:package { name="tomcat6" } -&gt;<br/>
     * new core:file { path = "${serverxml}" , source = lfile('tomcat6.xml.ftl',true) } -&gt;<br/>
     * new core:service { name='tomcat6' }<br/>
     * }
     * </code>
     *
     * @param path     Path to the file.
     * @param encoding encoding
     * @return file url.
     */
    @Method("lfile")
    public String createLibraryFileUrl(@Param("path") String path, @Param("encoding") @Default("UTF-8") String encoding) {
        if (!path.startsWith("/")) {
            String sourceUrl = STContext.get().getSourceUrl();
            if (sourceUrl != null) {
                String urlStr = sourceUrl.toString();
                int idx = urlStr.lastIndexOf('/');
                if (idx == -1) {
                    throw new IllegalArgumentException("Invalid source path (no '/' found): " + path);
                }
                path = urlStr.substring(0, idx + 1) + path;
            }
        }
        return "libfile:" + path;
    }

    /**
     * Create an url to a user file (files which looked up in any of the configured filestore locations).
     *
     * @param path        File path.
     * @param url         Optional URL from where the file can be retrieved
     * @param sha1        Optional SHA1 checksum (in hex format)
     * @param retrievable Flag indicating if the file is retrievable using the URL (If a URL is specified and this flag
     *                    is false, automatic retrieval will not happen and the user will be requested to manually
     *                    download and put the file in a filestore location).
     * @param encoding    File encoding.
     * @return file URL.
     */
    @Method("ufile")
    public String createUserFileUrl(@Param("path") String path, @Param("url") String url,
                                    @Param("sha1") String sha1, @Param("retrievable") @Default("true") boolean retrievable,
                                    @Param("encoding") @Default("UTF-8") String encoding) {
        return "ufile:" + path + "?encoding=" + urlEncode(encoding);
    }

    public class LocalDataFile extends DataFile {
        private URL url;

        public LocalDataFile(String url) throws MalformedURLException {
            this.url = new URL(url);
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public InputStream getStream() throws IOException {
            return url.openStream();
        }

        @Override
        public byte[] getSha1() throws IOException {
            try (InputStream stream = getStream()) {
                return CryptoUtils.sha1(stream);
            }
        }
    }

    public class FSDataFile extends DataFile {
        private FileDefinition def;
        private boolean temp;
        private String filename;
        private byte[] sha1;
        private boolean cpfile;
        private File local;

        public FSDataFile(@NotNull FileDefinition def) throws IOException {
            this.def = def;
            if (def.getPath() == null) {
                if ((def.getUrl() == null || !def.isRetrievable())) {
                    throw new IOException("DataFile path not set, nor it is retrievable file");
                } else {
                    temp = true;
                    filename = def.getUrl();
                }
            }
            if (isNotEmpty(def.getSha1())) {
                sha1 = Hex.decode(def.getSha1());
            }
            if (temp) {
                local = new TempFile("sttempfile", "tmp");
                STContext.get().registerTempFile(local);
            } else {
                cpfile = findInClasspath();
                local = findUserManaged();
            }
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public synchronized InputStream getStream() throws IOException {
            if (local != null) {
                byte[] localSha1 = CryptoUtils.sha1(local);
                if (sha1 != null && Arrays.equals(sha1, localSha1)) {
                    StringBuilder err = new StringBuilder("Local data file ").append(filename)
                            .append(" did not match sha1 checksum (is ").append(new String(Hex.encode(localSha1)))
                            .append(" but expected ").append(def.getSha1());
                    if (isNotEmpty(def.getUrl())) {
                        err.append(" retrieving file from URL instead");
                        logger.warn(err.toString());
                        local = null;
                    } else {
                        throw new IOException(err.toString());
                    }
                }
            }
            if (temp || local == null) {
                if (local == null) {
                    local = new File(locations.iterator().next() + File.separator + def.getPath().replace('/', File.separatorChar));
                }
                if (!def.isRetrievable()) {
                    StringBuilder err = new StringBuilder("Data File not and not retrievable");
                    if (isNotEmpty(def.getUrl())) {
                        err.append(", please download it from ").append(def.getUrl())
                                .append(" and store it in any of your file store locations under the path ");
                    } else {
                        err.append(": ");
                    }
                    err.append(def.getPath());
                    throw new IOException(err.toString());
                }
                // create parent dir for local if required
                File parentFile = local.getParentFile();
                if (!parentFile.exists()) {
                    if (!parentFile.mkdirs()) {
                        throw new IOException("Unable to create directory " + parentFile.getPath());
                    }
                }
                // retrieve file
                StringBuilder msg = new StringBuilder("retrieving file from url ").append(def.getUrl());
                if (!temp) {
                    msg.append(" and storing it at ").append(local.getPath());
                }
                logger.info(msg.toString());
                GetMethod getMethod = new GetMethod(def.getUrl());
                getMethod.setFollowRedirects(true);
                int retCode = httpClient.executeMethod(getMethod);
                if (retCode != HttpStatus.SC_OK) {
                    String statusText = getMethod.getStatusText();
                    if (statusText == null) {
                        statusText = "";
                    }
                    throw new IOException("Retrieving " + def.getUrl() + " failed with error " + retCode + " " + statusText);
                }
                try (FileOutputStream os = new FileOutputStream(local)) {
                    IOUtils.copy(getMethod.getResponseBodyAsStream(), os);
                }
                if (sha1 != null) {
                    byte[] localSha1 = CryptoUtils.sha1(local);
                    if (Arrays.equals(sha1, localSha1)) {
                        throw new IOException("Retrieved file did not match sha1 checksum (is " +
                                new String(Hex.encode(localSha1)) + " but expected " + def.getSha1());
                    }
                }
            } else {
                if (sha1 != null) {
                    byte[] localSha1 = CryptoUtils.sha1(local);
                    if (Arrays.equals(sha1, localSha1)) {
                        throw new IOException("File did not match sha1 checksum, and is not retrievable (is " +
                                new String(Hex.encode(localSha1)) + " but expected " + def.getSha1());
                    }
                }
            }
            if (def.isTemplate()) {
                StringWriter tmp = new StringWriter();
                try (FileReader fr = new FileReader(local)) {
                    Configuration cfg = new Configuration();
                    Template template = new Template(def.getPath(), fr, cfg, def.getEncoding());
                    HashMap<String, Object> vars = new HashMap<>();
                    try {
                        template.process(vars, tmp);
                        byte[] bytes = tmp.toString().getBytes(def.getEncoding());
                        sha1 = CryptoUtils.sha1(bytes);
                        return new ByteArrayInputStream(bytes);
                    } catch (TemplateException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                }
            } else {
                if (def.getSha1() != null) {
                    sha1 = Hex.decode(def.getSha1());
                } else {
                    try (FileInputStream is = new FileInputStream(this.local)) {
                        sha1 = CryptoUtils.sha1(is);
                    }
                }
                return new FileInputStream(this.local);
            }
        }

        @Override
        public byte[] getSha1() throws IOException {
            return CryptoUtils.sha1(getStream());
        }

        public File findUserManaged() {
            for (String location : locations) {
                File file = new File(location + File.separator + def.getPath().replace('/', File.separatorChar));
                if (file.exists()) {
                    return file;
                }
            }
            return null;
        }

        public boolean findInClasspath() throws IOException {
            String path = def.getPath();
            if (!path.startsWith("/")) {
                if (def.getSourceUrl() != null) {
                    String urlStr = def.getSourceUrl().toString();
                    int idx = urlStr.lastIndexOf('/');
                    if (idx == -1) {
                        throw new IOException("Invalid source path (no '/' found): " + path);
                    }
                    path = urlStr.substring(0, idx + 1) + path;
                }
            }
            URL resource = STContext.get().getLibraryClassloader().getResource(path);
            return resource != null;
        }
    }
}
