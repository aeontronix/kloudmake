/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild.resource.core;

import com.aeontronix.aeonbuild.annotation.*;
import com.aeontronix.aeonbuild.exception.InvalidQueryException;
import com.aeontronix.aeonbuild.exception.KMRuntimeException;
import com.aeontronix.aeonbuild.FQName;
import com.aeontronix.aeonbuild.BuildContextImpl;
import com.aeontronix.aeonbuild.Resource;
import com.aeontronix.aeonbuild.host.FileInfo;
import com.aeontronix.aeonbuild.host.Host;
import com.aeontronix.aeonbuild.service.filestore.DataFile;
import com.aeontronix.aeonbuild.service.filestore.FileStore;
import com.kloudtek.kryptotek.Digest;
import com.kloudtek.kryptotek.DigestAlgorithm;
import com.kloudtek.kryptotek.DigestOutputStream;
import com.kloudtek.kryptotek.DigestUtils;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.io.IOUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

@KMResource
@Dependency(value = "type core.xmlfile", optional = true)
public class FileResource {
    private static final Logger logger = LoggerFactory.getLogger(FileResource.class);
    private static final byte[] EMPTYSTRSHA1 = DigestUtils.sha1(new byte[0]);
    @Inject
    private BuildContextImpl ctx;
    @Service
    protected FileStore fileStore;
    @Inject
    protected Resource resource;
    @Inject
    protected Host host;
    @Attr
    protected String id;
    @Attr
    protected Ensure ensure = Ensure.FILE;
    @Attr
    @NotEmpty
    protected String path;
    @Attr(value = "content")
    protected String contentStr;
    @Attr
    protected String source;
    @Attr
    protected String permissions;
    @Attr
    protected boolean force;
    @Attr
    protected String sha1;
    @Attr
    protected boolean recursive;
    @Attr
    protected String owner;
    @Attr
    protected String group;
    @Attr
    protected boolean template;
    @Attr
    protected String target;
    @Attr
    protected boolean temporary;
    @Attr
    protected String templateResource;
    protected FileContent fileContent;
    private FileInfo finfo;
    private boolean delete;

    public FileResource() {
    }

    public FileResource(String path) {
        this.path = path;
    }

    @Verify("content")
    public boolean checkContent() throws KMRuntimeException, InvalidQueryException {
        delete = false;
        finfo = host.fileExists(path) ? host.getFileInfo(path) : null;
        switch (ensure) {
            case DIRECTORY:
                if (finfo != null) {
                    if (finfo.isDirectory()) {
                        return false;
                    } else {
                        if (force) {
                            delete = true;
                        } else {
                            logger.error("Unable to create directory {} because a file is present in it's place (use 'force' flag to have it replaced)", path);
                            throw new KMRuntimeException();
                        }
                    }
                }
                break;
            case FILE:
                loadContent();
                if (finfo != null) {
                    // there is an existing file
                    if (finfo.getType() == FileInfo.Type.FILE) {
                        byte[] existingFileSha1 = host.getFileSha1(path);
                        if (Arrays.equals(existingFileSha1, fileContent.getSha1())) {
                            logger.debug("File {} has the correct content", path);
                            return false;
                        }
                    } else {
                        if (force) {
                            delete = true;
                        } else {
                            logger.error("Unable to create file {} because a directory is present in it's place (use 'force' flag to have it replaced)", path);
                            throw new KMRuntimeException();
                        }
                    }
                }
                break;
            case SYMLINK:
                if (StringUtils.isEmpty(target)) {
                    throw new KMRuntimeException("file " + id + " is SYMLINK but target is not set");
                }
                if (finfo != null) {
                    if (finfo.isSymlink()) {
                        if (target.equals(finfo.getLinkTarget())) {
                            return false;
                        } else {
                            delete = true;
                        }
                    } else {
                        if (force) {
                            delete = true;
                        } else {
                            logger.error("Unable to create directory {} because a file is present in it's place (use 'force' flag to have it replaced)", path);
                            throw new KMRuntimeException();
                        }
                    }
                }
                break;
            case ABSENT:
                if (finfo == null) {
                    return false;
                }
                break;
            default:
                throw new KMRuntimeException(ensure + " not supported");
        }
        return true;
    }

    private void loadContent() throws KMRuntimeException, InvalidQueryException {
        if( StringUtils.isEmpty(path) ) {
            throw new KMRuntimeException("file resource path not set");
        }
        // find all file fragments
        StringBuffer query = new StringBuffer( "is samehost ");
        Iterator<FileFragmentDef> fileFragIt = fileStore.getFileFragmentDefs().iterator();
        if( fileFragIt.hasNext() ) {
            query.append("and ( ");
            while (fileFragIt.hasNext()) {
                FileFragmentDef fileFragmentDef = fileFragIt.next();
                query.append("type ").append(fileFragmentDef.getResourceType());
                if (fileFragIt.hasNext()) {
                    query.append(" or ");
                }
            }
            query.append(" ) ");
        }
        query.append("and @path eq '").append(path).append("'");
        List<Resource> fragments = ctx.findResources(query.toString(), resource);
        // If there are no file fragments, then let's create a binary file content object
        try {
            if (fragments.isEmpty()) {
                fileContent = new FileContentBinaryImpl();
            } else {
                FQName type = null;
                for (Resource fragment : fragments) {
                    if (fileContent == null) {
                        try {
                            type = fragment.getType();
                            fileContent = fileStore.getFileFragmentDef(type).getFileContentClass().newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new KMRuntimeException("Unable to instantiate file content class" + fileStore
                                    .getFileFragmentDef(type).getFileContentClass());
                        }
                    } else if (!type.equals(fragment.getType())) {
                        throw new KMRuntimeException("Found file fragments of different types for file " + id);
                    }
                }
            }
            if (isNotEmpty(contentStr)) {
                // Content attribute set is set, let's use that
                if (!isEmpty(source)) {
                    throw new KMRuntimeException("File " + path + " cannot have content as well as source specified");
                }
                byte[] data;
                try {
                    data = contentStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("BUG: UTF-8 not supported WTF ?!: " + e.getMessage(), e);
                }
                fileContent.init(id, new ByteArrayInputStream(data), DigestUtils.sha1(data));
            } else if (isEmpty(source)) {
                // Contact and Source not set, using the content currently in the file
                fileContent.init(id, host.readFile(path), host.getFileSha1(path));
            } else {
                // Using source
                try {
                    DataFile dataFile = fileStore.create(source);
                    if (template) {
                        Template templateObj = getTemplate(dataFile);
                        BuildContextImpl context = BuildContextImpl.get();
                        HashMap<String, Object> map = new HashMap<>();
                        Resource res;
                        if (templateResource != null) {
                            List<Resource> list = context.findResources(templateResource);
                            if (list.isEmpty()) {
                                throw new KMRuntimeException("Found no matches for templateResource: " + templateResource);
                            } else if (list.size() > 1) {
                                throw new KMRuntimeException("Found multiple matches for templateResource: " + templateResource);
                            } else {
                                res = list.iterator().next();
                            }
                        } else {
                            if (resource.getParent() != null) {
                                res = resource.getParent();
                            } else {
                                res = resource;
                            }
                        }
                        map.put("ctx", context);
                        map.put("res", res);
                        map.put("attrs", res.getAttributes());
                        map.put("vars", res.getVars());
                        Digest sha1Digest = DigestUtils.digest(DigestAlgorithm.SHA1);
                        ByteArrayOutputStream buf = new ByteArrayOutputStream();
                        try (Writer fw = new OutputStreamWriter(new DigestOutputStream(buf, sha1Digest))) {
                            templateObj.process(map, fw);
                        }
                        fileContent.init(id, new ByteArrayInputStream(buf.toByteArray()), sha1Digest.digest());
                    } else {
                        if (isNotEmpty(sha1)) {
                            try {
                                if (!Arrays.equals(Hex.decodeHex(sha1.toCharArray()), dataFile.getSha1())) {
                                    throw new KMRuntimeException("File found in the file store does not match specified sha1: " +
                                            sha1 + " was instead " + Hex.encodeHex(dataFile.getSha1()));
                                }
                            } catch (DecoderException e) {
                                throw new KMRuntimeException("Invalid SHA1 value (must be in hexadecimal format): " + sha1);
                            }
                        }
                        fileContent.init(id, dataFile.getStream(), dataFile.getSha1());
                    }
                } catch (TemplateException e) {
                    throw new KMRuntimeException("Invalid template file " + path + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new KMRuntimeException("Failed to read file " + path + ": " + e.getMessage(), e);
        }
        // Merge fragments into file content
        if (!fragments.isEmpty()) {
            fileContent.merge(fragments);
        }
    }

    private Template getTemplate(DataFile datafile) throws IOException {
        try (InputStream stream = datafile.getStream()) {
            String templatefile = IOUtils.toString(stream);
            Configuration cfg = new Configuration(Configuration.getVersion());
            StringTemplateLoader loader = new StringTemplateLoader();
            loader.putTemplate("template", templatefile);
            cfg.setTemplateLoader(loader);
            BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.getVersion()).build();
            wrapper.setSimpleMapWrapper(true);
            cfg.setObjectWrapper(wrapper);
            return cfg.getTemplate("template");
        }
    }

    private FQName checkFragmentType(List<Resource> fragments) throws KMRuntimeException {
        FQName type = fragments.get(0).getType();
        for (Resource fragment : fragments) {
            if (!fragment.getType().equals(type)) {
                throw new KMRuntimeException("File " + path + " has multiple fragment types");
            }
        }
        return type;
    }

    @Sync(value = "content", order = 1)
    public void syncContent() throws KMRuntimeException {
        // attempt to retrieve existing file metadata
        if (delete) {
            host.deleteFile(path, finfo.isDirectory());
        }
        switch (ensure) {
            case DIRECTORY:
                host.mkdir(path);
                logger.info("Created directory {}", path);
                break;
            case FILE:
                InputStream contentStream = fileContent.getStream();
                // TODO this is slightly insecure (file will be readable by others for a few milliseconds), FIX IT
                host.writeToFile(path, contentStream);
                logger.info("Updated file {} with content {sha1:{}}", path, Hex.encodeHex(fileContent.getSha1()));
                fileContent.close();
                break;
            case SYMLINK:
                host.createSymlink(path, target);
                logger.info("Created symlink {} targeting {}", path, target);
                break;
            case ABSENT:
                if (host.fileExists(path)) {
                    host.deleteFile(path, recursive);
                    logger.info("deleted {}", path);
                }
                break;
            default:
                throw new KMRuntimeException(ensure + " not supported");
        }
        assert host.fileExists(path);
        finfo = host.getFileInfo(path);
    }

    @Verify(value = "permissions")
    public boolean checkPermissions() {
        if (permissions != null) {
            return finfo != null && !finfo.getPermissions().equals(permissions);
        } else {
            return false;
        }
    }

    @Sync("permissions")
    public void syncPermissions() throws KMRuntimeException {
        host.setFilePerms(path, new FilePermissions(permissions));
        logger.info("Changed permissions of {} from {} to {}", path, finfo.getPermissions(), permissions);
    }

    @Verify(value = "owner")
    public boolean checkOwner() {
        if (owner != null) {
            return finfo != null && !owner.equals(finfo.getOwner());
        } else {
            return false;
        }
    }

    @Sync("owner")
    public void syncOwner() throws KMRuntimeException {
        host.setFileOwner(path, owner);
        logger.info("Changed owner of {} from {} to {}", path, finfo.getOwner(), owner);
    }

    @Verify(value = "group")
    public boolean checkGroup() {
        if (group != null) {
            return finfo != null && !group.equals(finfo.getGroup());
        } else {
            return false;
        }
    }

    @Sync("group")
    public void syncGroup() throws KMRuntimeException {
        host.setFileGroup(path, group);
        logger.info("Changed group of {} from {} to {}", path, finfo.getGroup(), group);
    }

    public enum Ensure {
        FILE, DIRECTORY, SYMLINK, ABSENT
    }
}
