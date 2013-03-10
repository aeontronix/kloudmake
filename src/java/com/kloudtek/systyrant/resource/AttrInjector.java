/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.annotation.Attr;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.STRuntimeException;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.Field;

import static com.kloudtek.util.StringUtils.isEmpty;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 10/03/13
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class AttrInjector extends Injector {
    private final String name;

    public AttrInjector(String name, Field field) {
        super(field);
        this.name = name;
    }

    @Override
    public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
        Class<?> fieldType = field.getType();
        String val = resource.get(name);
        if (val != null) {
            Object value;
            if (isEmpty(val)) {
                if (fieldType.isPrimitive()) {
                    value = ConvertUtils.convert(0, fieldType);
                } else {
                    value = null;
                }
            } else {
                if (fieldType.isEnum()) {
                    try {
                        value = Enum.valueOf((Class<? extends Enum>) fieldType, val.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new FieldInjectionException(field,"value "+val.toUpperCase()+" is not a valid enum for "+fieldType.getName(),e);
                    }
                } else {
                    value = ConvertUtils.convert(val, fieldType);
                }
            }
            inject(obj, value);
        }
    }
}
