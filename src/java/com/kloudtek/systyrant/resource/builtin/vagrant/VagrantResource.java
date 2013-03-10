/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.vagrant;

import com.kloudtek.systyrant.annotation.*;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.SshHost;
import org.apache.commons.io.FileUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@STResource
public class VagrantResource {
    private static final Logger logger = LoggerFactory.getLogger(VagrantResource.class);
    @Attr
    @NotEmpty
    private String box;
    @Attr
    @NotEmpty
    private String dir;
    @Attr
    private Ensure ensure = Ensure.UP;
    @Attr
    private Ensure after;
    @Service
    private Host host;
    @Service
    private ServiceManager serviceManager;
    @Resources
    private Collection<SharedFolder> sharedFolders;
    private SshHost sshHost;

    public VagrantResource() {
    }

    public VagrantResource(String box, String dir, Ensure ensure, Ensure after, Host host, ServiceManager serviceManager,
                           Collection<SharedFolder> sharedFolders) {
        this.box = box;
        this.dir = dir;
        this.ensure = ensure;
        this.after = after;
        this.host = host;
        this.serviceManager = serviceManager;
        this.sharedFolders = sharedFolders;
    }

    @Execute
    public void exec() throws STRuntimeException {
        String vagrantfile = "Vagrant::Config.run do |config|\n" +
                "  config.vm.box = \"" + box + "\"\n" +
                "end\n";
        String vagrantFilePath = dir + "/Vagrantfile";
        if (!host.fileExists(dir)) {
            logger.info("Creating vagrant dir {}", dir);
            host.mkdirs(dir);
        }
        if (!host.fileIsSame(vagrantFilePath, vagrantfile)) {
            logger.info("Updating vagrant config file {}", vagrantFilePath);
            host.writeToFile(vagrantFilePath, vagrantfile);
        }
        changeStatus(ensure);
        sshHost = createSshHost(host, dir);
        serviceManager.addOverride("host", sshHost);
        sshHost.start();
    }

    public static SshHost createSshHost(Host h, String vagrantDir) throws STRuntimeException {
        SshConfig sshConfig = new SshConfig(h.exec("cd " + vagrantDir + " && vagrant ssh-config"));
        logger.debug("Vagrant VM SSH config: {}", sshConfig);
        File pkeyFile = new File(sshConfig.getPkey());
        if (pkeyFile.exists() || pkeyFile.isFile()) {
            final byte[] privKey;
            try {
                privKey = FileUtils.readFileToByteArray(pkeyFile);
            } catch (IOException e) {
                throw new STRuntimeException("Error loading vagrant keyfile " + sshConfig.getPkey() + ": " + e.getMessage(), e);
            }
            SshHost sshHost = new SshHost();
            sshHost.setAddress(sshConfig.getHostname());
            sshHost.setPort(sshConfig.getPort());
            sshHost.setLoginUser(sshConfig.getUser());
            sshHost.setPrivKey(privKey);
            return sshHost;
        } else {
            throw new STRuntimeException("Vagrant keyfile does not exist or is a directory: " + sshConfig.getPkey());
        }
    }

    @Execute(postChildren = true)
    public void postChildrens() throws STRuntimeException {
        try {
            serviceManager.removeOverride("host", sshHost);
            host = (Host) serviceManager.getService("host");
            if (after != null) {
                changeStatus(after);
            }
        } catch (InvalidServiceException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }

    private void changeStatus(Ensure newState) throws STRuntimeException {
        Ensure status;
        String statusStr = host.exec("cd " + dir + " && vagrant status");
        if (statusStr.contains("The VM is powered off") || statusStr.contains("To resume this VM")) {
            status = Ensure.HALTED;
        } else if (statusStr.contains("The VM is running.")) {
            status = Ensure.UP;
        } else if (statusStr.contains("The environment has not yet been created.")) {
            status = Ensure.ABSENT;
        } else if (statusStr.contains("The VM is in an aborted state.")) {
            status = Ensure.ABORTED;
        } else {
            throw new STRuntimeException("Invalid vagrant status: " + statusStr);
        }
        if (!status.equals(newState)) {
            switch (newState) {
                case UP:
                    logger.info("Starting vagrant vm: {}", dir);
                    host.exec("cd " + dir + " && vagrant up");
                    break;
                case HALTED:
                    logger.info("Halting vagrant vm: {}", dir);
                    host.exec("cd " + dir + " && vagrant halt -f");
                    break;
                case DESTROYED:
                case ABSENT:
                    logger.info("Destroying vagrant vm: {}", dir);
                    host.exec("cd " + dir + " && vagrant destroy -f");
                    break;
                case ABORTED:
                    throw new STRuntimeException("ABORTED is not a valid state to set this VM to");
            }
        }
    }

    public SshHost getSshHost() {
        return sshHost;
    }

    public static class SshConfig {
        private static final Pattern REGEX_PORT = Pattern.compile("Port (\\d*)");
        private static final Pattern REGEX_PKEY = Pattern.compile("IdentityFile (.*)");
        private static final Pattern REGEX_USER = Pattern.compile("User (.*)");
        private static final Pattern REGEX_HOSTNAME = Pattern.compile("HostName (.*)");
        private String config;
        private String hostname;
        private int port;
        private String user;
        private String pkey;

        public SshConfig(String config) throws STRuntimeException {
            this.config = config;
            hostname = parse(REGEX_HOSTNAME);
            port = parseInt(REGEX_PORT);
            pkey = parse(REGEX_PKEY);
            user = parse(REGEX_USER);
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public String getUser() {
            return user;
        }

        public String getPkey() {
            return pkey;
        }

        @Override
        public String toString() {
            return "SshConfig{" +
                    "hostname='" + hostname + '\'' +
                    ", port=" + port +
                    ", user='" + user + '\'' +
                    ", pkey='" + pkey + '\'' +
                    '}';
        }

        private String parse(Pattern pattern) throws STRuntimeException {
            Matcher matcher = pattern.matcher(config);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new STRuntimeException("Invalid vagrant ssh-config: " + config);
            }
        }

        private int parseInt(Pattern pattern) throws STRuntimeException {
            try {
                return Integer.parseInt(parse(pattern));
            } catch (NumberFormatException e) {
                throw new STRuntimeException("Invalid vagrant ssh-config: " + config);
            }
        }
    }

    public enum Ensure {
        UP, HALTED, ABSENT, DESTROYED, ABORTED
    }
}
