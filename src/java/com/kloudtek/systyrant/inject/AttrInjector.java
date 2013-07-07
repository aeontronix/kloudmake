/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.inject;

import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.FieldInjectionException;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static com.kloudtek.util.StringUtils.isEmpty;

public class AttrInjector extends Injector {
    private static final Logger logger = LoggerFactory.getLogger(AttrInjector.class);
    private final String name;

    public AttrInjector(Class<?> clazz, String name, Field field) {
        super(clazz, field);
        this.name = name;
    }

    @SuppressWarnings("unchecked")
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
                        throw new FieldInjectionException(field, "value " + val.toUpperCase() + " is not a valid enum for " + fieldType.getName(), e);
                    }
                } else {
                    value = ConvertUtils.convert(val, fieldType);
                }
            }
            logger.trace("Injecting {} into {}", value, field);
            inject(obj, value);
        }
    }

    @Override
    public void updateAttr(Resource resource, Object obj) throws IllegalAccessException, InvalidAttributeException {
        Object newValObj = field.get(obj);
        String newVal = ConvertUtils.convert(newValObj);
        String oldVal = resource.get(name);
        if (oldVal == null && Boolean.FALSE.equals(newValObj)) {
            newVal = null;
        }
        if ((newVal == null && oldVal != null) || (newVal != null && !newVal.equals(oldVal))) {
            logger.debug("Updating resource {}'s attribute {} to {}", resource, name, newVal);
            resource.set(name, newVal);
        }
    }
}
