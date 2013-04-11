/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.core;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.annotation.STResource;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@STResource
public class XmlFileResource {
    @Attr(required = true)
    private String path;
    @Attr(required = true)
    private String type;
    @Attr
    private String attributes;
    @Attr
    private String parent;
    @Attr
    private String xpath;

    public static void sort(List<Resource> fragments) {
        Collections.sort(fragments, new Comparator<Resource>() {
            @Override
            public int compare(Resource o1, Resource o2) {
                String parent1 = o1.get("parent");
                String parent2 = o2.get("parent");
                if (parent1 == null && parent2 != null) {
                    return -1;
                } else if (parent1 != null && parent2 == null) {
                    return 1;
                } else {
                    return parent2.length() - parent1.length();
                }
            }
        });
    }

    public static void addToDoc(Resource resource, Document document) throws STRuntimeException {
        String type = resource.get("type");
        String parent = resource.get("parent");
        if (parent == null) {
            if (document.getChildNodes().getLength() > 0) {
                throw new STRuntimeException("Unable to add root element " + type + " as there already one: " + document.getChildNodes().item(0).getNodeName());
            }
            Element element = document.createElement("type");
        }
    }
}
