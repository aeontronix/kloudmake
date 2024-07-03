/*
 * Copyright (c) 2024 Aeontronix Inc
 */

package com.aeontronix.aeonbuild;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourceMatcher {
    private String pkg;
    private String name;

    public ResourceMatcher(@NotNull String pkg, String name) {
        this.pkg = pkg;
        this.name = name;
    }

    public ResourceMatcher(FQName fqName) {
        this.pkg = fqName.getPkg();
        this.name = fqName.getName();
    }

    public ResourceMatcher(String pkgWithOptionalName) {
        int idx = pkgWithOptionalName.indexOf(':');
        if (idx != -1) {
            pkg = pkgWithOptionalName.substring(0, idx);
            name = pkgWithOptionalName.substring(idx + 1, pkgWithOptionalName.length());
        } else {
            pkg = pkgWithOptionalName;
        }
    }

    public static List<ResourceMatcher> convert(List<String> matchers) {
        List<ResourceMatcher> list = new ArrayList<>();
        for (String matcher : matchers) {
            list.add(new ResourceMatcher(matcher));
        }
        return list;
    }

    public String getPkg() {
        return pkg;
    }

    public String getName() {
        return name;
    }

    public boolean matches(FQName fqname) {
        return fqname.getPkg().equals(pkg) && (name == null || fqname.getName().equals(name));
    }

    public static boolean matchAll(Collection<ResourceMatcher> importPaths, FQName fqName) {
        if (importPaths == null || fqName.getPkg().equals("default")) {
            return true;
        } else {
            for (ResourceMatcher importPath : importPaths) {
                if (importPath.matches(fqName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
