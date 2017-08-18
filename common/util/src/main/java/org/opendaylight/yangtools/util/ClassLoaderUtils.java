/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassLoaderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderUtils.class);
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Runs {@link Supplier} with provided {@link ClassLoader}.
     *
     * <p>
     * Invokes supplies function and makes sure that original {@link ClassLoader}
     * is context {@link ClassLoader} after execution.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be executed.
     * @return Result of supplier invocation.
     */
    public static <V> V withClassLoader(final ClassLoader cls, final Supplier<V> function) {
        requireNonNull(cls, "Classloader should not be null");
        requireNonNull(function, "Function should not be null");

        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(cls);
            return function.get();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    /**
     * Runs {@link Callable} with provided {@link ClassLoader}.
     *
     * <p>
     * Invokes supplies function and makes sure that original {@link ClassLoader}
     * is context {@link ClassLoader} after execution.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be executed.
     * @return Result of callable invocation.
     */
    public static <V> V withClassLoader(final ClassLoader cls, final Callable<V> function) throws Exception {
        requireNonNull(cls, "Classloader should not be null");
        requireNonNull(function, "Function should not be null");

        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(cls);
            return function.call();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    public static Object construct(final Constructor<?> constructor, final List<Object> objects)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Object[] initargs = objects.toArray();
        return constructor.newInstance(initargs);
    }

    /**
     * Loads class using this supplied classloader.
     *
     * @param name String name of class.
     */
    public static Class<?> loadClass(final ClassLoader cls, final String name) throws ClassNotFoundException {
        if ("byte[]".equals(name)) {
            return byte[].class;
        }
        if ("char[]".equals(name)) {
            return char[].class;
        }
        return loadClass0(cls,name);
    }

    private static Class<?> loadClass0(final ClassLoader cls, final String name) throws ClassNotFoundException {
        try {
            return cls.loadClass(name);
        } catch (final ClassNotFoundException e) {
            final List<String> components = DOT_SPLITTER.splitToList(name);

            if (isInnerClass(components)) {
                final int length = components.size() - 1;
                final String outerName = Joiner.on(".").join(components.subList(0, length));
                final String innerName = outerName + "$" + components.get(length);
                return cls.loadClass(innerName);
            }

            throw e;
        }
    }

    private static boolean isInnerClass(final List<String> components) {
        final int length = components.size();
        if (length < 2) {
            return false;
        }

        final String potentialOuter = components.get(length - 2);
        if (potentialOuter == null) {
            return false;
        }
        return Character.isUpperCase(potentialOuter.charAt(0));
    }

    public static Class<?> loadClassWithTCCL(final String name) throws ClassNotFoundException {
        return loadClass(Thread.currentThread().getContextClassLoader(), name);
    }

    public static Class<?> tryToLoadClassWithTCCL(final String fullyQualifiedName) {
        try {
            return loadClassWithTCCL(fullyQualifiedName);
        } catch (final ClassNotFoundException e) {
            LOG.debug("Failed to load class {}", fullyQualifiedName, e);
            return null;
        }
    }

    public static <S,G,P> Class<P> findFirstGenericArgument(final Class<S> scannedClass, final Class<G> genericType) {
        return withClassLoader(scannedClass.getClassLoader(), findFirstGenericArgumentTask(scannedClass, genericType));
    }

    @SuppressWarnings("unchecked")
    private static <S, G, P> Supplier<Class<P>> findFirstGenericArgumentTask(final Class<S> scannedClass,
            final Class<G> genericType) {
        return () -> {
            final ParameterizedType augmentationGeneric = findParameterizedType(scannedClass, genericType);
            if (augmentationGeneric != null) {
                return (Class<P>) augmentationGeneric.getActualTypeArguments()[0];
            }
            return null;
        };
    }

    public static ParameterizedType findParameterizedType(final Class<?> subclass, final Class<?> genericType) {
        requireNonNull(subclass);
        requireNonNull(genericType);

        for (final Type type : subclass.getGenericInterfaces()) {
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
