/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.host.FileInfo;
import com.kloudtek.systyrant.host.Host;
import com.kloudtek.systyrant.service.filestore.DataFile;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.XmlUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

@STResource
@Dependency(value = "type core:xmlfile", optional = true)
public class FileResource {
    private static final Logger logger = LoggerFactory.getLogger(FileResource.class);
    private static final byte[] EMPTYSTRSHA1 = CryptoUtils.sha1(new byte[0]);
    @Inject
    private STContext ctx;
    @Attr
    protected String id;
    @Inject
    protected FileStore fileStore;
    @Inject
    protected Host host;
    @Attr
    protected Ensure ensure = Ensure.FILE;
    @Attr
    @NotEmpty
    protected String path;
    @Attr
    protected String content;
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
    protected String target;
    @Attr
    protected boolean temporary;
    protected byte[] sha1Bytes;
    protected InputStream contentStream;
    private FileInfo finfo;
    private boolean delete;

    public FileResource() {
    }

    public FileResource(String path) {
        this.path = path;
    }

    @Verify("content")
    public boolean checkContent() throws STRuntimeException {
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
                            throw new STRuntimeException();
                        }
                    }
                }
                break;
            case FILE:
                return checkFile();
            case SYMLINK:
                if (StringUtils.isEmpty(target)) {
                    throw new STRuntimeException("file " + id + " is SYMLINK but target is not set");
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
                            throw new STRuntimeException();
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
                throw new STRuntimeException(ensure + " not supported");
        }
        return true;
    }

    private boolean checkFile() throws STRuntimeException {
        if (isNotEmpty(sha1)) {
            try {
                sha1Bytes = Hex.decodeHex(sha1.toCharArray());
            } catch (DecoderException e) {
                throw new STRuntimeException("Invalid SHA1 value (must be in hexadecimal format): " + sha1);
            }
        }
        findContent();
        if (finfo != null) {
            // there is an existing file
            if (finfo.getType() == FileInfo.Type.FILE) {
                byte[] existingFileSha1 = host.getFileSha1(path);
                if (sha1Bytes != null && Arrays.equals(existingFileSha1, sha1Bytes)) {
                    logger.debug("File {} has the correct content", path);
                    return false;
                }
            } else {
                if (force) {
                    delete = true;
                } else {
                    logger.error("Unable to create file {} because a directory is present in it's place (use 'force' flag to have it replaced)", path);
                    throw new STRuntimeException();
                }
            }
        }
        return true;
    }

    private void findContent() throws STRuntimeException {
        try {
            List<Resource> fragments = new ArrayList<>(ctx.findResources("samehost and type core:xmlfile"));
            if (!fragments.isEmpty()) {
                if (isNotEmpty(content) || isNotEmpty(source)) {
                    throw new STRuntimeException("File " + path + " cannot have fragments as well as content or source specified");
                }
                FQName type = checkFragmentType(fragments);
                if (type.toString().equals("core:xmlfile")) {
                    XmlFileResource.sort(fragments);
                    Document document = XmlUtils.createDocument();
                    for (Resource fragment : fragments) {
                        XmlFileResource.addToDoc(fragment, document);
                    }
                } else {
                    throw new STRuntimeException("BUG: file fragment type " + type);
                }
            }
        } catch (InvalidQueryException e) {
            throw new STRuntimeException("BUG: file fragment query invalid");
        }
        if (isNotEmpty(content)) {
            if (!isEmpty(source)) {
                throw new STRuntimeException("File " + path + " cannot have content as well as source specified");
            }
            byte[] data;
            try {
                data = content.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("BUG: UTF-8 not supported WTF ?!: " + e.getMessage(), e);
            }
            contentStream = new ByteArrayInputStream(data);
            if (sha1Bytes == null) {
                sha1Bytes = CryptoUtils.sha1(data);
            }
        } else if (isEmpty(source)) {
            contentStream = new ByteArrayInputStream(new byte[0]);
            sha1Bytes = EMPTYSTRSHA1;
        } else {
            try {
                DataFile dataFile = fileStore.get(path);
                contentStream = dataFile.getStream();
                sha1Bytes = dataFile.getSha1();
            } catch (IOException e) {
                throw new STRuntimeException("Failed to read file " + path + ": " + e.getMessage(), e);
            }
        }
    }

    private FQName checkFragmentType(List<Resource> fragments) throws STRuntimeException {
        FQName type = fragments.get(0).getType();
        for (Resource fragment : fragments) {
            if (!fragment.getType().equals(type)) {
                throw new STRuntimeException("File " + path + " has multiple fragment types");
            }
        }
        return type;
    }

    @Sync(value = "content", order = 1)
    public void syncContent() throws STRuntimeException {
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
                MessageDigest digest = CryptoUtils.digest(CryptoUtils.Algorithm.SHA1);
                if (sha1Bytes == null) {
                    contentStream = new DigestInputStream(contentStream, digest);
                }
                // TODO this is probably slightly insecure (might be readable by others), to fix later
                host.writeToFile(path, contentStream);
                if (sha1Bytes == null) {
                    sha1Bytes = digest.digest();
                }
                logger.info("Updated file {} with content {sha1:{}}", path, Hex.encodeHexString(sha1Bytes));
                break;
            case SYMLINK:
                host.createSymlink(path, target);
                logger.info("Created symlink {} targetting {}", path, target);
                break;
            case ABSENT:
                if (host.fileExists(path)) {
                    host.deleteFile(path, recursive);
                    logger.info("deleted {}", path);
                }
                break;
            default:
                throw new STRuntimeException(ensure + " not supported");
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
    public void syncPermissions() throws STRuntimeException {
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
    public void syncOwner() throws STRuntimeException {
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
    public void syncGroup() throws STRuntimeException {
        host.setFileGroup(path, group);
        logger.info("Changed group of {} from {} to {}", path, finfo.getGroup(), group);
    }

    public enum Ensure {
        FILE, DIRECTORY, SYMLINK, ABSENT
    }
}
