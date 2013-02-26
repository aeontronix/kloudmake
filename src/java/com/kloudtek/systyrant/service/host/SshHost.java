/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.service.host;

import com.jcraft.jsch.*;
import com.kloudtek.systyrant.ExecutionResult;
import com.kloudtek.systyrant.FileInfo;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kloudtek.systyrant.service.host.Host.Logging.NO;

/**
 * {@link com.kloudtek.systyrant.service.host.Host} implementation that uses SSH to remotely administer a server.
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
    private String username;

    public SshHost() {
    }

    public SshHost(String keyName, byte[] privKey, byte[] pubKey, byte[] passphrase, String username, String address, int port) throws JSchException {
        this.username = username;
        this.address = address;
        this.port = port;
        init(keyName, privKey, pubKey, passphrase, username);
    }

    public void init(String keyName, byte[] privKey, byte[] pubKey, byte[] passphrase, String username) throws JSchException {
        this.keyName = keyName;
        this.privKey = privKey;
        this.pubKey = pubKey;
        this.passphrase = passphrase;
        this.username = username;
    }

    @Override
    public void start() throws STRuntimeException {
        try {
            jsch = new JSch();
            jsch.addIdentity(keyName, privKey, pubKey, passphrase);
            session = jsch.getSession(this.username, this.address, this.port);
            session.setConfig("StrictHostKeyChecking", "no");
//        session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
//        session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            executor = new SshExecutor(session);
            execPrefix = "sudo ";
            rootUser = username.equals("root");
        } catch (JSchException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void doStop() {
        sftpChannel.disconnect();
        session.disconnect();
    }

    @Override
    protected CommandLine generateCommandLine(String command, boolean includePreSuFix) {
        return ExtendedCommandLine.parse(command, includePreSuFix, execPrefix, execSuffix);
    }

    @Override
    public void deleteFile(String path, boolean recursive) throws STRuntimeException {
        try {
            if (recursive) {
                exec("rm -rf " + path, 0, NO);
            } else {
                ExecutionResult res = exec("rm -f " + path, null, NO);
                if (res.getErrCode() != 0) {
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
        return result.getErrCode() == 0;
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
        String tmpfile = createTempFile();
        try {
            sftpChannel.put(dataStream, tmpfile, ChannelSftp.OVERWRITE);
        } catch (SftpException e) {
            throw new STRuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(dataStream);
        }
        try {
            exec("mv " + tmpfile + " " + path, defaultTimeout, defaultSuccessRetCode, NO, true);
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
        exec("chown " + owner);
    }

    @Override
    public void setFileGroup(String path, String group) throws STRuntimeException {
        exec("chown :" + group);
    }

    @Override
    public String createTempDir() throws STRuntimeException {
        String tempdir = exec("mktemp -d", getDefaultTimeout(), getDefaultSuccessRetCode(), getDefaultLogging(), false).getOutput().trim();
        tempDirs.add(tempdir);
        return tempdir;
    }

    @Override
    public String createTempFile() throws STRuntimeException {
        String tmpfile = exec("mktemp", getDefaultTimeout(), getDefaultSuccessRetCode(), getDefaultLogging(), false).getOutput().trim();
        tempFiles.add(tmpfile);
        return tmpfile;
    }

    @Override
    public ExecutionResult execScript(String script, ScriptType type, long timeout, @Nullable Integer expectedRetCode, Logging logging, boolean includePreSuFix) throws STRuntimeException {
        String tmpFile = createTempFile();
        try {
            writeToFile(tmpFile, script);
            switch (type) {
                case BASH:
                    String cmd = "bash " + tmpFile;
                    logger.debug("executing: {}", cmd);
                    return exec(cmd, timeout, expectedRetCode, logging, includePreSuFix);
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
            return username.equalsIgnoreCase("root");
        }
    }

    public void setRootUser(boolean rootUser) {
        this.rootUser = rootUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public static SshHost createSSHAdminForVagrantInstance(String ip, int port, String user) throws JSchException {
        byte[] privKey = "-----BEGIN RSA PRIVATE KEY-----\nMIIEogIBAAKCAQEA6NF8iallvQVp22WDkTkyrtvp9eWW6A8YVr+kz4TjGYe7gHzI\nw+niNltGEFHzD8+v1I2YJ6oXevct1YeS0o9HZyN1Q9qgCgzUFtdOKLv6IedplqoP\nkcmF0aYet2PkEDo3MlTBckFXPITAMzF8dJSIFo9D8HfdOV0IAdx4O7PtixWKn5y2\nhMNG0zQPyUecp4pzC6kivAIhyfHilFR61RGL+GPXQ2MWZWFYbAGjyiYJnAmCP3NO\nTd0jMZEnDkbUvxhMmBYSdETk1rRgm+R4LOzFUGaHqHDLKLX+FIPKcF96hrucXzcW\nyLbIbEgE98OHlnVYCzRdK8jlqm8tehUc9c9WhQIBIwKCAQEA4iqWPJXtzZA68mKd\nELs4jJsdyky+ewdZeNds5tjcnHU5zUYE25K+ffJED9qUWICcLZDc81TGWjHyAqD1\nBw7XpgUwFgeUJwUlzQurAv+/ySnxiwuaGJfhFM1CaQHzfXphgVml+fZUvnJUTvzf\nTK2Lg6EdbUE9TarUlBf/xPfuEhMSlIE5keb/Zz3/LUlRg8yDqz5w+QWVJ4utnKnK\niqwZN0mwpwU7YSyJhlT4YV1F3n4YjLswM5wJs2oqm0jssQu/BT0tyEXNDYBLEF4A\nsClaWuSJ2kjq7KhrrYXzagqhnSei9ODYFShJu8UWVec3Ihb5ZXlzO6vdNQ1J9Xsf\n4m+2ywKBgQD6qFxx/Rv9CNN96l/4rb14HKirC2o/orApiHmHDsURs5rUKDx0f9iP\ncXN7S1uePXuJRK/5hsubaOCx3Owd2u9gD6Oq0CsMkE4CUSiJcYrMANtx54cGH7Rk\nEjFZxK8xAv1ldELEyxrFqkbE4BKd8QOt414qjvTGyAK+OLD3M2QdCQKBgQDtx8pN\nCAxR7yhHbIWT1AH66+XWN8bXq7l3RO/ukeaci98JfkbkxURZhtxV/HHuvUhnPLdX\n3TwygPBYZFNo4pzVEhzWoTtnEtrFueKxyc3+LjZpuo+mBlQ6ORtfgkr9gBVphXZG\nYEzkCD3lVdl8L4cw9BVpKrJCs1c5taGjDgdInQKBgHm/fVvv96bJxc9x1tffXAcj\n3OVdUN0UgXNCSaf/3A/phbeBQe9xS+3mpc4r6qvx+iy69mNBeNZ0xOitIjpjBo2+\ndBEjSBwLk5q5tJqHmy/jKMJL4n9ROlx93XS+njxgibTvU6Fp9w+NOFD/HvxB3Tcz\n6+jJF85D5BNAG3DBMKBjAoGBAOAxZvgsKN+JuENXsST7F89Tck2iTcQIT8g5rwWC\nP9Vt74yboe2kDT531w8+egz7nAmRBKNM751U/95P9t88EDacDI/Z2OwnuFQHCPDF\nllYOUI+SpLJ6/vURRbHSnnn8a/XG+nzedGH5JGqEJNQsz+xT2axM0/W/CRknmGaJ\nkda/AoGANWrLCz708y7VYgAtW2Uf1DPOIYMdvo6fxIB5i9ZfISgcJ/bbCUkFrhoH\n+vq/5CIWxCPp0f85R4qxxQ5ihxJ0YDQT9Jpx4TMss4PSavPaBH3RXow5Ohe+bYoQ\nNE5OgEXk2wVfZczCZpigBKbKZHNYcelXtTt/nP3rsCuGcM4h53s=\n-----END RSA PRIVATE KEY-----".getBytes();
        byte[] pubKey = "ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA6NF8iallvQVp22WDkTkyrtvp9eWW6A8YVr+kz4TjGYe7gHzIw+niNltGEFHzD8+v1I2YJ6oXevct1YeS0o9HZyN1Q9qgCgzUFtdOKLv6IedplqoPkcmF0aYet2PkEDo3MlTBckFXPITAMzF8dJSIFo9D8HfdOV0IAdx4O7PtixWKn5y2hMNG0zQPyUecp4pzC6kivAIhyfHilFR61RGL+GPXQ2MWZWFYbAGjyiYJnAmCP3NOTd0jMZEnDkbUvxhMmBYSdETk1rRgm+R4LOzFUGaHqHDLKLX+FIPKcF96hrucXzcWyLbIbEgE98OHlnVYCzRdK8jlqm8tehUc9c9WhQ== vagrant insecure public key\n".getBytes();
        return new SshHost("test", privKey, pubKey, null, user, ip, port);
    }
}
