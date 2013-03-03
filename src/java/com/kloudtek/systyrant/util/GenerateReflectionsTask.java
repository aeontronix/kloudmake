/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.reflections.Reflections;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GenerateReflectionsTask extends Task {
    private File destfile;
    private File classes;
    private String pkgs;

    @Override
    public void execute() throws BuildException {
        if (classes == null) {
            throw new BuildException("attribute 'classes' missing.");
        }
        if (pkgs == null) {
            throw new BuildException("attribute 'pkgs' missing.");
        }
        if (destfile == null) {
            destfile = new File(classes.getPath() + File.separator + "META-INF" + File.separator + "systyrant-reflections");
        }
        ArrayList<String> pkglist = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(pkgs, ",");
        while (tok.hasMoreElements()) {
            pkglist.add(tok.nextToken());
        }
        try {
            File parentFile = destfile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            new Reflections(new ConfigurationBuilder().setUrls(classes.toURI().toURL()).setSerializer(new XmlSerializer()))
                    .save(destfile.getPath());
            log("Generate reflections file " + destfile.getPath());
        } catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }

    public void setClasses(File classes) {
        this.classes = classes;
    }

    public void setPkgs(String pkgs) {
        this.pkgs = pkgs;
    }

    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }
}
