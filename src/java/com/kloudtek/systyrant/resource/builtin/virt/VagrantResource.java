/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.builtin.virt;

import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.Execute;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.service.ServiceManager;
import com.kloudtek.systyrant.service.host.Host;
import com.kloudtek.systyrant.service.host.SshHost;
import org.apache.commons.io.FileUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@STResource()
public class VagrantResource {
    private static final Logger logger = LoggerFactory.getLogger(VagrantResource.class);
    private static final Pattern REGEX_PORT = Pattern.compile("Port (\\d*)");
    private static final Pattern REGEX_PKEY = Pattern.compile("IdentityFile (.*)");
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
    @Resource
    private Host host;
    @Resource
    private ServiceManager serviceManager;
    private SshHost sshHost;

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
        String sshConfig = h.exec("cd " + vagrantDir + " && vagrant ssh-config");
        int port = getPortFromConfig(sshConfig);
        logger.debug("Vagrant VM SSH port is {}", port);
        String pkeyPath = getPKeyFromConfig(sshConfig);
        logger.debug("Vagrant VM keyfile is {}", pkeyPath);
        File pkeyFile = new File(pkeyPath);
        if (pkeyFile.exists() || pkeyFile.isFile()) {
            final byte[] privKey;
            try {
                privKey = FileUtils.readFileToByteArray(pkeyFile);
            } catch (IOException e) {
                throw new STRuntimeException("Error loading vagrant keyfile " + pkeyPath + ": " + e.getMessage(), e);
            }
            SshHost sshHost = new SshHost();
            sshHost.setAddress("127.0.0.1");
            sshHost.setPort(port);
            sshHost.setUsername("vagrant");
            sshHost.setPrivKey(privKey);
            return sshHost;
        } else {
            throw new STRuntimeException("Vagrant keyfile does not exist or is a directory: " + pkeyPath);
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

    private static int getPortFromConfig(String sshConfig) throws STRuntimeException {
        Matcher sshPortMatcher = REGEX_PORT.matcher(sshConfig);
        if (sshPortMatcher.find()) {
            try {
                return Integer.parseInt(sshPortMatcher.group(1));
            } catch (NumberFormatException e) {
                throw new STRuntimeException("Invalid vagrant ssh-config: " + sshConfig);
            }
        } else {
            throw new STRuntimeException("Invalid vagrant ssh-config: " + sshConfig);
        }
    }

    private static String getPKeyFromConfig(String sshConfig) throws STRuntimeException {
        Matcher sshPortMatcher = REGEX_PKEY.matcher(sshConfig);
        if (sshPortMatcher.find()) {
            return sshPortMatcher.group(1);
        } else {
            throw new STRuntimeException("Invalid vagrant ssh-config: " + sshConfig);
        }
    }

    @STResource
    public class SharedFolder {

    }

    public enum Ensure {
        UP, HALTED, ABSENT, DESTROYED, ABORTED
    }
}
