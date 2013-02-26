/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.core;

import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.filestore.DataFile;
import com.kloudtek.systyrant.service.filestore.FileStore;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.util.CryptoUtils;
import com.kloudtek.util.StringUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import static com.kloudtek.systyrant.FileInfo.Type.SYMLINK;
import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

@STResource
public class FileResource {
    private static final Logger logger = LoggerFactory.getLogger(FileResource.class);
    private static final byte[] EMPTYSTRSHA1 = CryptoUtils.sha1(new byte[0]);
    @Resource
    protected FileStore fileStore;
    @Resource
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
    protected String sourceRetrievable;
    @Attr
    protected String mode;
    @Attr
    protected boolean force;
    @Attr
    protected String sha1;
    @Attr
    protected boolean skipStreamVerification = false;
    @Attr
    protected boolean recursive;
    @Attr
    protected String target;
    @Attr
    protected boolean temporary;
    protected byte[] sha1Bytes;
    protected InputStream contentStream;

    public FileResource() {
    }

    public FileResource(String path) {
        this.path = path;
    }

    @Execute
    public void execute() throws STRuntimeException {
        Host admin = host;
        FileInfo finfo = null;
        // attempt to retrieve existing file metadata
        if (admin.fileExists(path)) {
            finfo = admin.getFileInfo(path);
        }
        switch (ensure) {
            case DIRECTORY:
                createDirectory(admin, finfo);
                break;
            case FILE:
                writeFile(admin, finfo);
                break;
            case SYMLINK:
                createSymlink(admin, finfo);
                break;
            case ABSENT:
                if (admin.fileExists(path)) {
                    admin.deleteFile(path, recursive);
                    logger.info("deleted {}", path);
                }
                break;
            default:
                throw new STRuntimeException(ensure + " not supported");
        }
    }

    private void createSymlink(Host admin, FileInfo finfo) throws STRuntimeException {
        if (StringUtils.isEmpty(target)) {
            throw new STRuntimeException("file is SYMLINK but target is not set");
        }
        if (finfo != null) {
            if (finfo.getType() == SYMLINK && finfo.getLinkTarget().equalsIgnoreCase(target)) {
                return;
            }
            if (finfo.getType() == SYMLINK || force) {
                admin.deleteFile(path, false);
            } else {
                logger.error("Unable to create symlink {} because a file or directory is present in it's place (use 'force' flag to have it replaced)", path);
                throw new STRuntimeException();
            }
        }
        admin.createSymlink(path, target);
    }

    private void writeFile(Host admin, FileInfo finfo) throws STRuntimeException {
        if (isNotEmpty(sha1)) {
            try {
                sha1Bytes = Hex.decodeHex(sha1.toCharArray());
            } catch (DecoderException e) {
                throw new STRuntimeException("Invalid SHA1 value (must be in hexadecimal format): " + sha1);
            }
        }
        MessageDigest digest = CryptoUtils.digest(CryptoUtils.Algorithm.SHA1);
        if (isNotEmpty(content)) {
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
        if (finfo != null) {
            // there is an existing file
            if (finfo.getType() == FileInfo.Type.FILE) {
                byte[] existingFileSha1 = admin.getFileSha1(path);
                if (sha1Bytes != null && Arrays.equals(existingFileSha1, sha1Bytes)) {
                    logger.debug("DataFile {} has the correct content", path);
                    return;
                }
            } else {
                if (force) {
                    admin.deleteFile(path, false);
                } else {
                    logger.error("Unable to create file {} because a directory is present in it's place (use 'force' flag to have it replaced)", path);
                    throw new STRuntimeException();
                }
            }
        }
        if (sha1Bytes == null) {
            contentStream = new DigestInputStream(contentStream, digest);
        }
        host.writeToFile(path, contentStream);
        if (sha1Bytes == null) {
            sha1Bytes = digest.digest();
        }
        logger.info("Updated file {} with content {sha1:{}}", path, Hex.encodeHexString(sha1Bytes));
    }

    private void createDirectory(Host admin, FileInfo finfo) throws STRuntimeException {
        if (finfo != null) {
            if (finfo.getType() == FileInfo.Type.OTHER) {
                if (force) {
                    admin.deleteFile(path, false);
                } else {
                    logger.error("Unable to create directory {} because a file is present in it's place (use 'force' flag to have it replaced)", path);
                    throw new STRuntimeException();
                }
            } else {
                return;
            }
        }
        admin.mkdir(path);
        logger.info("Created directory {}", path);
    }

    public enum Ensure {
        FILE, DIRECTORY, SYMLINK, ABSENT
    }
}
