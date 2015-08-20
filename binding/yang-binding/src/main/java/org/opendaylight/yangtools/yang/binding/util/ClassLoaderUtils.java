/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use {@link org.opendaylight.yangtools.util.ClassLoaderUtils} instead.
 */
@Deprecated
public final class ClassLoaderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderUtils.class);

    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     *
     * Runs {@link Supplier} with provided {@link ClassLoader}.
     *
     * Invokes supplies function and makes sure that original {@link ClassLoader}
     * is context {@link ClassLoader} after execution.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be executed.
     * @return Result of supplier invocation.
     *
     */
    public static <V> V withClassLoader(final ClassLoader cls, final Supplier<V> function) {
        checkNotNull(cls, "Classloader should not be null");
        checkNotNull(function, "Function should not be null");

        final ClassLoader oldCls = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls);
            return function.get();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCls);
        }
    }

    /**
     *
     * Runs {@link Callable} with provided {@link ClassLoader}.
     *
     * Invokes supplies function and makes sure that original {@link ClassLoader}
     * is context {@link ClassLoader} after execution.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be executed.
     * @return Result of callable invocation.
     *
     */
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

    /**
     *
     * Runs {@link Callable} with provided {@link ClassLoader} and Lock.
     *
     * Invokes supplies function after acquiring lock
     * and makes sure that original {@link ClassLoader}
     * is context {@link ClassLoader} and lock is unlocked
     * after execution.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be executed.
     * @return Result of Callable invocation.
     *
     */
    public static <V> V withClassLoaderAndLock(final ClassLoader cls, final Lock lock, final Supplier<V> function) {
        checkNotNull(lock, "Lock should not be null");

        lock.lock();
        try {
            return withClassLoader(cls, function);
        } finally {
            lock.unlock();
        }
    }

    public static Object construct(final Constructor<? extends Object> constructor, final List<Object> objects)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Object[] initargs = objects.toArray();
        return constructor.newInstance(initargs);
    }

    /**
     *
     * Loads class using this supplied classloader.
     *
     *
     * @param cls
     * @param name String name of class.
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(final ClassLoader cls, final String name) throws ClassNotFoundException {
        if ("byte[]".equals(name)) {
            return byte[].class;
        }
        if ("char[]".equals(name)) {
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
            LOG.debug("Failed to load class {}", fullyQualifiedName, e);
            return null;
        }
    }

    public static <S,G,P> Class<P> findFirstGenericArgument(final Class<S> scannedClass, final Class<G> genericType) {
        return withClassLoader(scannedClass.getClassLoader(), ClassLoaderUtils.<S,G,P>findFirstGenericArgumentTask(scannedClass, genericType));
    }

    private static <S,G,P> Supplier<Class<P>> findFirstGenericArgumentTask(final Class<S> scannedClass, final Class<G> genericType) {
        return new Supplier<Class<P>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Class<P> get() {
                final ParameterizedType augmentationGeneric = findParameterizedType(scannedClass, genericType);
                if (augmentationGeneric != null) {
                    return (Class<P>) augmentationGeneric.getActualTypeArguments()[0];
                }
                return null;
            }
        };
    }

    public static ParameterizedType findParameterizedType(final Class<?> subclass, final Class<?> genericType) {
        Preconditions.checkNotNull(subclass);
        Preconditions.checkNotNull(genericType);

        for (Type type : subclass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType && genericType.equals(((ParameterizedType) type).getRawType())) {
                return (ParameterizedType) type;
            }
        }

        LOG.debug("Class {} does not declare interface {}", subclass, genericType);
        return null;
    }

    public static Type getFirstGenericParameter(final Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        return null;
    }
}
