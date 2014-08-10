/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.resource.core;

import com.kloudtek.kloudmake.exception.InvalidAttributeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePermissions {
    private static Pattern permRegex = Pattern.compile("^(d|-)?(r|-)(w|-)(x|-)(r|-)(w|-)(x|-)(r|-)(w|-)(x|-)$");
    private boolean ownerRead;
    private boolean ownerWrite;
    private boolean ownerExecute;
    private boolean groupRead;
    private boolean groupWrite;
    private boolean groupExecute;
    private boolean otherRead;
    private boolean otherWrite;
    private boolean otherExecute;

    public FilePermissions() {
    }

    public FilePermissions(String perms) throws InvalidAttributeException {
        Matcher matcher = permRegex.matcher(perms);
        if (matcher.find()) {
            ownerRead = matcher.group(2).equals("r");
            ownerWrite = matcher.group(3).equals("w");
            ownerExecute = matcher.group(4).equals("x");
            groupRead = matcher.group(5).equals("r");
            groupWrite = matcher.group(6).equals("w");
            groupExecute = matcher.group(7).equals("x");
            otherRead = matcher.group(8).equals("r");
            otherWrite = matcher.group(9).equals("w");
            otherExecute = matcher.group(10).equals("x");
        } else {
            throw new InvalidAttributeException("Invalid permissions " + perms);
        }
    }

    public boolean isOwnerRead() {
        return ownerRead;
    }

    public void setOwnerRead(boolean ownerRead) {
        this.ownerRead = ownerRead;
    }

    public boolean isOwnerWrite() {
        return ownerWrite;
    }

    public void setOwnerWrite(boolean ownerWrite) {
        this.ownerWrite = ownerWrite;
    }

    public boolean isOwnerExecute() {
        return ownerExecute;
    }

    public void setOwnerExecute(boolean ownerExecute) {
        this.ownerExecute = ownerExecute;
    }

    public boolean isGroupRead() {
        return groupRead;
    }

    public void setGroupRead(boolean groupRead) {
        this.groupRead = groupRead;
    }

    public boolean isGroupWrite() {
        return groupWrite;
    }

    public void setGroupWrite(boolean groupWrite) {
        this.groupWrite = groupWrite;
    }

    public boolean isGroupExecute() {
        return groupExecute;
    }

    public void setGroupExecute(boolean groupExecute) {
        this.groupExecute = groupExecute;
    }

    public boolean isOtherRead() {
        return otherRead;
    }

    public void setOtherRead(boolean otherRead) {
        this.otherRead = otherRead;
    }

    public boolean isOtherWrite() {
        return otherWrite;
    }

    public void setOtherWrite(boolean otherWrite) {
        this.otherWrite = otherWrite;
    }

    public boolean isOtherExecute() {
        return otherExecute;
    }

    public void setOtherExecute(boolean otherExecute) {
        this.otherExecute = otherExecute;
    }

    public String toChmodString() {
        return new StringBuilder().append("u=").append(ownerRead ? "r" : "").append(ownerWrite ? "w" : "").append(ownerExecute ? "x" : "")
                .append(",g=").append(groupRead ? "r" : "").append(groupWrite ? "w" : "").append(groupExecute ? "x" : "")
                .append(",o=").append(otherRead ? "r" : "").append(otherWrite ? "w" : "").append(otherExecute ? "x" : "").toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(ownerRead ? "r" : "-").append(ownerWrite ? "w" : "-").append(ownerExecute ? "x" : "-")
                .append(groupRead ? "r" : "-").append(groupWrite ? "w" : "-").append(groupExecute ? "x" : "-")
                .append(otherRead ? "r" : "-").append(otherWrite ? "w" : "-").append(otherExecute ? "x" : "-").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilePermissions)) return false;

        FilePermissions that = (FilePermissions) o;

        if (groupExecute != that.groupExecute) return false;
        if (groupRead != that.groupRead) return false;
        if (groupWrite != that.groupWrite) return false;
        if (otherExecute != that.otherExecute) return false;
        if (otherRead != that.otherRead) return false;
        if (otherWrite != that.otherWrite) return false;
        if (ownerExecute != that.ownerExecute) return false;
        if (ownerRead != that.ownerRead) return false;
        if (ownerWrite != that.ownerWrite) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (ownerRead ? 1 : 0);
        result = 31 * result + (ownerWrite ? 1 : 0);
        result = 31 * result + (ownerExecute ? 1 : 0);
        result = 31 * result + (groupRead ? 1 : 0);
        result = 31 * result + (groupWrite ? 1 : 0);
        result = 31 * result + (groupExecute ? 1 : 0);
        result = 31 * result + (otherRead ? 1 : 0);
        result = 31 * result + (otherWrite ? 1 : 0);
        result = 31 * result + (otherExecute ? 1 : 0);
        return result;
    }
}
