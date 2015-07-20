/*
 * Copyright (c) 2015. Kelewan Technologies Ltd
 */

package com.kloudtek.kloudmake;

import com.kloudtek.kloudmake.annotation.KMResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.kloudtek.util.StringUtils.isEmpty;
import static com.kloudtek.util.StringUtils.isNotEmpty;

/**
 * Represents a fully qualified resource name.
 */
public class FQName {
    private String pkg;
    private String name;

    public FQName() {
    }

    public FQName(@NotNull Class<?> clazz) {
        this(clazz, null);
    }

    public FQName(@NotNull Class<?> clazz, FQName fqname) {
        KMResource rsAnno = clazz.getAnnotation(KMResource.class);
        boolean annoNameDef = rsAnno != null && isNotEmpty(rsAnno.value());
        int annoNameSep = rsAnno != null ? rsAnno.value().indexOf(".") : -1;
        if (fqname != null) {
            name = fqname.getName();
        } else {
            if (annoNameDef) {
                if (annoNameSep != -1) {
                    name = rsAnno.value().substring(annoNameSep + 1);
                } else {
                    name = rsAnno.value();
                }
            } else {
                name = clazz.getSimpleName().toLowerCase();
                if (name.endsWith("resource")) {
                    name = name.substring(0, name.length() - 8);
                }
            }
        }
        if (fqname != null && fqname.getPkg() != null) {
            pkg = fqname.getPkg();
        } else {
            if (annoNameSep != -1) {
                pkg = rsAnno.value().substring(0, annoNameSep);
            } else {
                Package jpkg = clazz.getPackage();
                KMResource pkgAnno = jpkg.getAnnotation(KMResource.class);
                if (pkgAnno != null && isNotEmpty(pkgAnno.value())) {
                    pkg = pkgAnno.value();
                } else {
                    pkg = jpkg.getName();
                }
            }
        }
    }

    public FQName(@NotNull String fqname) {
        int idx = fqname.lastIndexOf(".");
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

    public boolean matches(FQName fqname, KMContextImpl ctx) {
        if (isNotEmpty(pkg)) {
            return equals(fqname);
        } else {
            for (String importValue : ctx.getImports()) {
                boolean fullyQualified = importValue.indexOf('.') != -1;
                String fqnameStr = fqname.toString();
                String compare = fullyQualified ? importValue : importValue + "." + name;
                if (compare.equalsIgnoreCase(fqnameStr)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        if (isEmpty(pkg)) {
            return name;
        } else {
            return pkg + "." + name;
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
        return !(pkg != null ? !pkg.equals(fqName.pkg) : fqName.pkg != null);

    }

    @Override
    public int hashCode() {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }
}
