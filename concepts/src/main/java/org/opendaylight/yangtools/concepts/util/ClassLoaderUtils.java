/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts.util;

import static org.opendaylight.yangtools.concepts.util.ClassLoaderUtils.findParameterizedType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;


public final class ClassLoaderUtils {

    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <V> V withClassLoader(ClassLoader cls, Callable<V> function) throws Exception {
        return withClassLoaderAndLock(cls, null, function);
    }

    public static <V> V withClassLoaderAndLock(ClassLoader cls, Lock lock, Callable<V> function) throws Exception {
        if (cls == null) {
            throw new IllegalArgumentException("Classloader should not be null");
        }
        if (function == null) {
            throw new IllegalArgumentException("Function should not be null");
        }

        if (lock != null) {
            lock.lock();
        }
        ClassLoader oldCls = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls);
            return function.call();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCls);
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    public static ParameterizedType findParameterizedType(Class<?> subclass, Class<?> genericType) {
        if(subclass == null || genericType == null) {
            throw new IllegalArgumentException("Class was not specified.");
        }
        for (Type type : subclass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType && genericType.equals(((ParameterizedType) type).getRawType())) {
                return (ParameterizedType) type;
            }
        }
        return null;
    }

    public static <S,G,P> Class<P> findFirstGenericArgument(final Class<S> scannedClass, final Class<G> genericType) {
        try {
            return withClassLoader(scannedClass.getClassLoader(), ClassLoaderUtils.<S,G,P>findFirstGenericArgumentTask(scannedClass, genericType));
        } catch (Exception e) {
            return null;
        }
    }
    
    private static <S,G,P> Callable<Class<P>> findFirstGenericArgumentTask(final Class<S> scannedClass, final Class<G> genericType) {
        return new Callable<Class<P>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Class<P> call() throws Exception {
                final ParameterizedType augmentationGeneric = findParameterizedType(scannedClass,
                        genericType);
                if (augmentationGeneric == null) {
                    return null;
                }
                return (Class<P>) augmentationGeneric.getActualTypeArguments()[0];
            }
        };
    }

}