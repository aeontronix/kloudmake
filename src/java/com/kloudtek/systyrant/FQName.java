/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.kloudtek.util.StringUtils.isEmpty;

/** Represents a fully qualified resource name. */
public class FQName {
    private String pkg;
    private String name;

    public FQName() {
    }

    public FQName(@NotNull String fqname) {
        int idx = fqname.lastIndexOf(":");
        if (idx != -1) {
            pkg = fqname.substring(0, idx);
            name = fqname.substring(idx + 1, fqname.length());
        } else {
            name = fqname;
        }
    }

    public FQName(@Nullable String namespace, @NotNull String name) {
        this.pkg = namespace;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(@Nullable String pkg) {
        this.pkg = pkg;
    }

    @Override
    public String toString() {
        if (isEmpty(pkg)) {
            return name;
        } else {
            return pkg + ":" + name;
        }
    }

    public boolean equals(@NotNull String pkg, @NotNull String name) {
        return this.pkg.equalsIgnoreCase(pkg) && this.name.equals(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FQName)) return false;

        FQName fqName = (FQName) o;

        if (!name.equals(fqName.name)) return false;
        if (pkg != null ? !pkg.equals(fqName.pkg) : fqName.pkg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }
}
