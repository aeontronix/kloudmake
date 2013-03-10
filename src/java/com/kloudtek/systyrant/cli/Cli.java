/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.service.host.SshHost;
import com.kloudtek.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.hibernate.validator.internal.util.Version;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.*;

/** Used to launch SysTyrant through command line */
public class Cli {
    public static final Pattern RGXSSH = Pattern.compile("(\\w*@)?(.*?)(:\\d*)");
    private static final Logger logger = LoggerFactory.getLogger(Cli.class);
    @Parameter(description = "files")
    private List<String> definitions;
    @Parameter(description = "Libraries directory", names = {"-l", "--libs"})
    private List<String> moduleDirs;
    @Parameter(description = "Enable debug logging", names = {"-d", "--debug"})
    private boolean debug;
    @Parameter(description = "Use SSH as default host, with the specified address in format of [user@]host[:port]", names = {"-ssh"})
    private String ssh;
    @Parameter(description = "SSH Key location (defaults to ~/.ssh/id_rsa)", names = {"-sshkey"})
    private String sshKey = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa";

    public void execute() {
        try {
            STContext context = new STContext();
            if (StringUtils.isNotEmpty(ssh)) {
                Matcher sshMatcher = RGXSSH.matcher(ssh);
                if (!sshMatcher.find()) {
                    logger.error("Invalid ssh value: " + ssh);
                }
                String user = sshMatcher.group(1);
                if (user == null) {
                    user = "root";
                } else {
                    user = user.substring(0, user.length() - 1);
                }
                String addr = sshMatcher.group(2);
                int port = 22;
                String portVal = sshMatcher.group(3);
                if (portVal != null) {
                    try {
                        port = Integer.parseInt(portVal.substring(1));
                    } catch (NumberFormatException e) {
                        logger.error("Invalid port: " + portVal.substring(1));
                    }
                }
                File sshPrivKeyFile = new File(sshKey);
                if (!sshPrivKeyFile.exists()) {
                    logger.error("Unable to find key file: " + sshPrivKeyFile.getPath());
                    return;
                }
                byte[] sshPrivKey = FileUtils.readFileToByteArray(sshPrivKeyFile);
                SshHost sshHost = new SshHost(sshPrivKeyFile.getPath(), sshPrivKey, null, null, user, addr, port);
                context.getServiceManager().assignService("host", sshHost);
            }
            if (moduleDirs != null) {
                for (String dir : moduleDirs) {
                    context.registerLibraries(new File(dir));
                }
            }
            for (String definition : definitions) {
                context.runScript(URI.create(definition));
            }
            context.execute();
        } catch (Exception e) {
            logger.error("An unexpected error has occured: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Cli cli = new Cli();
        JCommander jc = new JCommander(cli, args);
        jc.setProgramName("systyrant");
        if (cli.definitions == null || cli.definitions.isEmpty()) {
            jc.usage();
        } else {
            configureLogging(cli.debug ? DEBUG : INFO);
            cli.execute();
        }
    }


    @SuppressWarnings("unchecked")
    public static void configureLogging(Level level) {
        ch.qos.logback.classic.Logger valverlog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Version.class);
        valverlog.setLevel(ERROR);
        ch.qos.logback.classic.Logger reflog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Reflections.class);
        reflog.setLevel(ERROR);
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT");
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ConsoleAppender console = (ConsoleAppender) logger.getAppender("console");
        logger.setLevel(level);
        console.stop();
        PatternLayout pl = new PatternLayout();
        pl.setPattern("%r %5p [%X{resource}] %m%n)");
        pl.setContext(lc);
        pl.start();
        console.setLayout(pl);
        console.start();
    }
}
