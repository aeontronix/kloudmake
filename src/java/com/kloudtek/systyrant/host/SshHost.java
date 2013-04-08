/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.host;

import com.jcraft.jsch.*;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.resource.builtin.core.FilePermissions;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kloudtek.systyrant.host.Host.Logging.NO;

/**
 * {@link com.kloudtek.systyrant.host.Host} implementation that uses SSH to remotely administer a server.
 */
public class SshHost extends AbstractHost {
    private static final Logger logger = LoggerFactory.getLogger(SshHost.class);
    private JSch jsch;
    private String address;
    private int port;
    private Session session;
    private ChannelSftp sftpChannel;
    private Boolean rootUser;
    private String keyName;
    private byte[] privKey;
    private byte[] pubKey;
    private byte[] passphrase;
    private String loginUser;
    private String defaultUser = "root";
    private boolean started;

    public SshHost() {
        handleQuoting = true;
    }

    public SshHost(String keyName, byte[] privKey, byte[] pubKey, byte[] passphrase, String loginUser, String address, int port) throws JSchException {
        handleQuoting = true;
        this.loginUser = loginUser;
        this.address = address;
        this.port = port;
        init(keyName, privKey, pubKey, passphrase, loginUser);
    }

    public void init(String keyName, byte[] privKey, byte[] pubKey, byte[] passphrase, String username) throws JSchException {
        this.keyName = keyName;
        this.privKey = privKey;
        this.pubKey = pubKey;
        this.passphrase = passphrase;
        this.loginUser = username;
    }

    @Override
    public void doStart() throws STRuntimeException {
        try {
            jsch = new JSch();
            jsch.addIdentity(keyName, privKey, pubKey, passphrase);
            session = jsch.getSession(this.loginUser, this.address, this.port);
            session.setConfig("StrictHostKeyChecking", "no");
            //        session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
            //        session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            executor = new SshExecutor(session);
            rootUser = loginUser.equals("root");
            started = true;
        } catch (JSchException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
        super.doStart();
    }

    @Override
    public void doStop() {
        sftpChannel.disconnect();
        session.disconnect();
        started = false;
    }

    @Override
    public void deleteFile(String path, boolean recursive) throws STRuntimeException {
        try {
            if (recursive) {
                exec("rm -rf " + path, 0, NO);
            } else {
                ExecutionResult res = exec("rm -f " + path, null, NO);
                if (res.getRetCode() != 0) {
                    exec("rmdir " + path, 0, NO);
                }
            }
        } catch (STRuntimeException e) {
            throw new STRuntimeException("Unable to delete " + path + ": " + e.getLocalizedMessage());
        }
    }

    @NotNull
    @Override
    public FileInfo getFileInfo(String path) throws STRuntimeException {
        try {
            FileInfo fileInfo = new FileInfo(path, exec(FileInfo.UNIX_STAT_CMD + path));
            if (fileInfo.getType() == FileInfo.Type.SYMLINK) {
                fileInfo.setLinkTarget(exec("readlink " + path).trim());
            }
            return fileInfo;
        } catch (STRuntimeException e) {
            throw new STRuntimeException("Failed to retrieve file info for " + path + ": " + e.getMessage());
        }
    }

    @Override
    public boolean fileExists(String path) throws STRuntimeException {
        ExecutionResult result = exec("test -e " + path, null, NO);
        return result.getRetCode() == 0;
    }

    @Override
    public String getFilePathSeparator() {
        return "/";
    }

    @Override
    public boolean mkdir(String path) throws STRuntimeException {
        if (isRootUser()) {
            try {
                sftpChannel.mkdir(path);
                return true;
            } catch (SftpException e) {
                if (e.id == 2) {
                    throw new STRuntimeException("Unable to create directory " + path + ", parent directory missing");
                } else {
                    throw new STRuntimeException("Unable to create directory " + path + ": " + e.getLocalizedMessage());
                }
            }
        } else {
            if (!fileExists(path)) {
                exec("mkdir " + path);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean mkdirs(String path) throws STRuntimeException {
        if (!fileExists(path)) {
            exec("mkdir -p " + path);
            return true;
        }
        return false;
    }

    @Override
    public byte[] getFileSha1(String path) throws STRuntimeException {
        String result = exec("sha1sum " + path, getDefaultSuccessRetCode(), NO).getOutput();
        Matcher matcher = Pattern.compile("(\\S*?)\\s*" + path + "\n").matcher(result);
        if (!matcher.find()) {
            throw new STRuntimeException("Invalid checksum returned by sha1sum: " + result);
        }
        String checksum = matcher.group(1);
        try {
            return Hex.decodeHex(checksum.toCharArray());
        } catch (DecoderException e) {
            throw new STRuntimeException("Error while retrieving file sha1: " + e.getMessage());
        }
    }

    @Override
    public void writeToFile(String path, byte[] data) throws STRuntimeException {
        writeToFile(path, new ByteArrayInputStream(data));
    }

    @Override
    public byte[] readFile(String path) throws STRuntimeException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            // TODO handle files non-root user can't read
            sftpChannel.get(path, buf);
            return buf.toByteArray();
        } catch (SftpException e) {
            throw new STRuntimeException("Unable to read file " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void writeToFile(String path, InputStream dataStream) throws STRuntimeException {
        logger.debug("Writing stream to {} via SSH");
        String tmpfile = exec("mktemp", getDefaultTimeout(), getDefaultSuccessRetCode(), getDefaultLogging(), loginUser).getOutput().trim();
        logger.debug("Created temporary file {} on host {}", tmpfile);
        tempFiles.add(tmpfile);
        try {
            sftpChannel.put(dataStream, tmpfile, ChannelSftp.OVERWRITE);
        } catch (SftpException e) {
            throw new STRuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(dataStream);
        }
        try {
            exec("mv " + tmpfile + " " + path, defaultTimeout, defaultSuccessRetCode, NO, null);
            logger.debug("moved temporary file {} to final destination {}", tmpfile, path);
        } catch (RuntimeException | STRuntimeException e) {
            try {
                if (fileExists(tmpfile)) {
                    deleteFile(tmpfile, false);
                }
            } catch (STRuntimeException e1) {
                logger.warn("Error while attempting to delete temporary file: " + e.getLocalizedMessage());
            }
            throw e;
        }
    }

    @Override
    public void createSymlink(String path, String target) throws STRuntimeException {
        try {
            exec("ln -s " + target + " " + path);
        } catch (STRuntimeException e) {
            throw new STRuntimeException("Unable to create symlink: " + path + ": " + e.getMessage());
        }
    }

    @Override
    public void setFileOwner(String path, String owner) throws STRuntimeException {
        exec("chown " + owner + " " + path);
    }

    @Override
    public void setFileGroup(String path, String group) throws STRuntimeException {
        exec("chown :" + group + " " + path);
    }

    @Override
    public void setFilePerms(String path, FilePermissions perms) throws STRuntimeException {
        exec("chmod " + perms.toChmodString() + " " + path);
    }

    @Override
    public String createTempDir() throws STRuntimeException {
        String tempdir = exec("mktemp -d", getDefaultTimeout(), getDefaultSuccessRetCode(), getDefaultLogging(), null).getOutput().trim();
        tempDirs.add(tempdir);
        return tempdir;
    }

    @Override
    public String createTempFile() throws STRuntimeException {
        String tmpfile = exec("mktemp", getDefaultTimeout(), getDefaultSuccessRetCode(), getDefaultLogging(), null).getOutput().trim();
        tempFiles.add(tmpfile);
        return tmpfile;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @NotNull
    @Override
    public ExecutionResult exec(String command, @Nullable Long timeout, @Nullable Integer expectedRetCode, Logging logging, String user, String workdir, @Nullable Map<String, String> env) throws STRuntimeException {
        if (env == null || env.isEmpty()) {
            return super.exec(command, timeout, expectedRetCode, logging, user, workdir, env);
        } else {
            StringBuilder script = new StringBuilder("#!/bin/bash\n\n");
            for (Map.Entry<String, String> entry : env.entrySet()) {
                script.append("export ").append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
            }
            script.append('\n').append(command);
            return execScript(script.toString(), ScriptType.BASH, timeout, expectedRetCode, logging, user);
        }
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, String user) throws STRuntimeException {
        String tmpFile = createTempFile();
        try {
            writeToFile(tmpFile, script);
            switch (type) {
                case BASH:
                    String cmd = "bash " + tmpFile;
                    logger.debug("executing: {}", cmd);
                    return exec(cmd, timeout, expectedRetCode, logging, null);
                default:
                    throw new STRuntimeException("Unsupported script type: " + type.toString());
            }
        } finally {
            deleteFile(tmpFile, false);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public boolean isRootUser() {
        if (rootUser != null) {
            return rootUser;
        } else {
            return loginUser.equalsIgnoreCase("root");
        }
    }

    public void setRootUser(boolean rootUser) {
        this.rootUser = rootUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public byte[] getPrivKey() {
        return privKey;
    }

    public void setPrivKey(byte[] privKey) {
        this.privKey = privKey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(byte[] passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public String getCurrentUser() {
        return loginUser;
    }

    @Override
    public String getDefaultUser() {
        return defaultUser;
    }

    @Override
    protected boolean execSupportsWorkDir() {
        return false;
    }

    public void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    @Override
    public String toString() {
        return "SSH Host to " + address + " #" + hashCode();
    }
}
