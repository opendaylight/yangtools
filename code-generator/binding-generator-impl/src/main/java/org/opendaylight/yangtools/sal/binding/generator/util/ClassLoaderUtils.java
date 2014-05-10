/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

public final class ClassLoaderUtils {

    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <V> V withClassLoader(final ClassLoader cls, final Callable<V> function) throws Exception {
        checkNotNull(cls, "Classloader should not be null");
        checkNotNull(function, "Function should not be null");

        final ClassLoader oldCls = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls);
            return function.call();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCls);
        }
    }

    public static <V> V withClassLoaderAndLock(final ClassLoader cls, final Lock lock, final Callable<V> function) throws Exception {
        checkNotNull(lock, "Lock should not be null");

        lock.lock();
        try {
            return withClassLoader(cls, function);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @deprecated Use one of the other utility methods.
     */
    @Deprecated
    public static <V> V withClassLoaderAndLock(final ClassLoader cls, final Optional<Lock> lock, final Callable<V> function) throws Exception {
        if (lock.isPresent()) {
            return withClassLoaderAndLock(cls, lock.get(), function);
        } else {
            return withClassLoader(cls, function);
        }
    }

    public static Object construct(final Constructor<? extends Object> constructor, final List<Object> objects)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object[] initargs = objects.toArray(new Object[] {});
        return constructor.newInstance(initargs);
    }


    public static Class<?> loadClass(final ClassLoader cls, final String name) throws ClassNotFoundException {
        if ("byte[]".equals(name)) {
            return byte[].class;
        } else if("char[]".equals(name)) {
            return char[].class;
        }
        try {
            return cls.loadClass(name);
        } catch (ClassNotFoundException e) {
            String[] components = name.split("\\.");
            String potentialOuter;
            int length = components.length;
            if (length > 2 && (potentialOuter = components[length - 2]) != null && Character.isUpperCase(potentialOuter.charAt(0))) {

                    String outerName = Joiner.on(".").join(Arrays.asList(components).subList(0, length - 1));
                    String innerName = outerName + "$" + components[length-1];
                    return cls.loadClass(innerName);
            } else {
                throw e;
            }
        }
    }

    public static Class<?> loadClassWithTCCL(final String name) throws ClassNotFoundException {
        return loadClass(Thread.currentThread().getContextClassLoader(), name);
    }

    public static Class<?> tryToLoadClassWithTCCL(final String fullyQualifiedName) {
        try {
            return loadClassWithTCCL(fullyQualifiedName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
