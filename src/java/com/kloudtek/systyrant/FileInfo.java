/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInfo {
    public static final String UNIX_STAT_CMD = "stat -c '%F:%s:%A:%Y:%U:%G' ";
    private static final Pattern UNIX_STAT_CMD_PATTERN = Pattern.compile("(.*?):(\\d*?):([rwxdl-]*?):(\\d*?):(.*?):(.*)");
    private String path;
    private Type type;
    private long size;
    private String permissions;
    private long modified;
    private String owner;
    private String group;
    private String linkTarget;

    public FileInfo() {
    }

    public FileInfo(String path) {
        this.path = path;
    }

    public FileInfo(String path, String stats) {
        Matcher matcher = UNIX_STAT_CMD_PATTERN.matcher(stats);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid file stats: " + stats);
        }
        this.path = path;
        switch (matcher.group(1)) {
            case "symbolic link":
                type = Type.SYMLINK;
                break;
            case "directory":
                type = Type.DIRECTORY;
                break;
            case "regular file":
            case "regular empty file":
                type = Type.FILE;
                break;
            default:
                type = Type.OTHER;
                break;
        }
        size = Long.parseLong(matcher.group(2));
        permissions = matcher.group(3);
        modified = Long.parseLong(matcher.group(4)) * 1000;
        owner = matcher.group(5);
        group = matcher.group(6);
    }

    public FileInfo(String path, Type type) {
        this.path = path;
        this.type = type;
    }

    public FileInfo(String path, Type type, long size, String permissions, long modified, String owner, String group, String linkTarget) {
        this.path = path;
        this.type = type;
        this.size = size;
        this.permissions = permissions;
        this.modified = modified;
        this.owner = owner;
        this.group = group;
        this.linkTarget = linkTarget;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    public boolean isDirectory() {
        return type != null && type == Type.DIRECTORY;
    }

    public boolean isFile() {
        return type != null && type == Type.FILE;
    }

    public boolean isSymlink() {
        return type != null && type == Type.SYMLINK;
    }

    public boolean isOther() {
        return type != null && type == Type.OTHER;
    }

    public enum Type {
        FILE, DIRECTORY, SYMLINK, OTHER
    }
}
