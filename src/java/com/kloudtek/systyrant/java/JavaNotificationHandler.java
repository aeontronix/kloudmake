/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.java;

import com.kloudtek.systyrant.Notification;
import com.kloudtek.systyrant.NotificationHandler;
import com.kloudtek.systyrant.Resource;
import com.kloudtek.systyrant.annotation.HandleNotification;
import com.kloudtek.systyrant.exception.STRuntimeException;
import com.kloudtek.systyrant.util.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 31/03/13
 * Time: 00:54
 * To change this template use File | Settings | File Templates.
 */
public class JavaNotificationHandler extends NotificationHandler {
    private Method method;
    private Class<?> implClass;

    public JavaNotificationHandler(Method method, HandleNotification anno, Class<?> implClass) {
        super(anno.reorder(), anno.aggregate(), anno.onlyIfAfter(), anno.value());
        this.method = method;
        this.implClass = implClass;
    }

    @Override
    public void handleNotification(Notification notification) throws STRuntimeException {
        Resource resource = notification.getTarget();
        try {
            method.invoke(resource.getJavaImpl(implClass));
        } catch (IllegalAccessException e) {
            throw new STRuntimeException("Unable to invoke " + ReflectionHelper.toString(method));
        } catch (InvocationTargetException e) {
            throw new STRuntimeException(e.getMessage(), e);
        }
    }
}
