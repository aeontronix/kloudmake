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
import com.kloudtek.systyrant.exception.InvalidServiceException;
import com.kloudtek.systyrant.host.SshHost;
import com.kloudtek.systyrant.service.credstore.CredStore;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.UnableToDecryptException;
import org.apache.commons.io.FileUtils;
import org.hibernate.validator.internal.util.Version;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qos.logback.classic.Level.*;

/**
 * Used to launch SysTyrant through command line
 */
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
    @Parameter(description = "SSH Key location", names = {"-sshkey"})
    private String sshKey = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa";
    @Parameter(description = "Credentials file should be encrypted", names = {"-c", "--crypt"})
    private boolean crypt;
    @Parameter(description = "File where credentials will be store (will default to the first script filename appended with '.creds') ", names = {"-cf", "--credsfile"})
    private File credsfile;
    @Parameter(description = "File containing encryption password (also enables encryption as -c)", names = {"-cpw", "--cryptpw"})
    private File cryptPw;
    @Parameter(description = "Enable java remote debugging on port 5005", names = "-jdebug")
    private boolean jdebug;

    public int execute() {
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
                    return 3;
                }
                byte[] sshPrivKey = FileUtils.readFileToByteArray(sshPrivKeyFile);
                SshHost sshHost = new SshHost(sshPrivKeyFile.getPath(), sshPrivKey, null, null, user, addr, port);
                context.getServiceManager().assignService("host", sshHost);
            }
            registerLibs(context);
            CredStore credStore = configureCredStore(context);
            for (String definition : definitions) {
                try {
                    context.runScriptFile(URI.create(definition));
                } catch (IOException e) {
                    logger.error("Failed to read script " + definition + " : " + e.getMessage(), e);
                } catch (ScriptException e) {
                    if (e.getCause() != null) {
                        logger.error(e.getCause().getMessage(), e);
                    } else {
                        logger.error("An error occured while executing script " + definition + " : " + e.getMessage(), e);
                    }
                    return 5;
                }
            }
            boolean successful = context.execute();
            if (credsfile != null) {
                credStore.save(new FileOutputStream(credsfile));
            }
            return successful ? 0 : 1;
        } catch (Exception e) {
            logger.error("An unexpected error has occured: " + e.getMessage(), e);
            return 2;
        }
    }

    private void registerLibs(STContext context) {
        if (moduleDirs == null) {
            moduleDirs = new ArrayList<>();
        }
        addLib("/etc/systyrant/libs");
        addLib("/var/lib/systyrant/libs");
        for (String dir : moduleDirs) {
            context.registerLibraries(new File(dir));
        }
    }

    private void addLib(String s) {
        if (new File(s).exists()) {
            moduleDirs.add(s);
        }
    }

    private CredStore configureCredStore(STContext context) throws InvalidServiceException, IOException, UnableToDecryptException {
        CredStore credStore = CredStore.get(context);
        if (cryptPw != null) {
            crypt = true;
            if (cryptPw.exists()) {
                logger.error("Encryption password file " + cryptPw + " does not exist");
            }
            credStore.setCryptPw(FileUtils.readFileToString(cryptPw));
        }
        if (credsfile != null && credsfile.exists()) {
            credStore.load(new FileInputStream(credsfile));
        }
        return credStore;
    }

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    public static int execute(String... args) {
        Cli cli = new Cli();
        JCommander jc = new JCommander(cli, args);
        jc.setProgramName("systyrant");
        if (cli.definitions == null || cli.definitions.isEmpty()) {
            jc.usage();
            return 5;
        } else {
            configureLogging(cli.debug ? DEBUG : INFO);
            return cli.execute();
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
        pl.setPattern("%r %5p [%X{resource}] %m%n%nopex");
        pl.setContext(lc);
        pl.start();
        console.setLayout(pl);
        console.start();
    }
}
