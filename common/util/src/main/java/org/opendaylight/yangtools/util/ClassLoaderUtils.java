/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with ClassLoaders and classes.
 */
public final class ClassLoaderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderUtils.class);
    private static final Joiner DOT_JOINER = Joiner.on(".");
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    private ClassLoaderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Immediately call {@link Function#apply(Object)} with provided {@link ClassLoader}. This method safely switches
     * the thread's Thread Context Class Loader to the specified class loader for the duration of execution of that
     * method.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be applied.
     * @param input Function input
     * @throws NullPointerException if class loader or function is null
     */
    @Beta
    public static <T, R> R applyWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Function<T, R> function,
            final T input) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(requireNonNull(cls));
        try {
            return requireNonNull(function).apply(input);
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    /**
     * Immediately call {@link Callable#call()} with provided {@link ClassLoader}. This method safely switches
     * the thread's Thread Context Class Loader to the specified class loader for the duration of execution of that
     * method.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param callable Function to be executed.
     * @return Result of callable invocation.
     * @throws NullPointerException if class loader or callable is null
     */
    @Beta
    public static <V> V callWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Callable<V> callable)
            throws Exception {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(requireNonNull(cls));
        try {
            return requireNonNull(callable).call();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    /**
     * Immediately call {@link Supplier#get()} with provided {@link ClassLoader}. This method safely switches
     * the thread's Thread Context Class Loader to the specified class loader for the duration of execution of that
     * method.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param supplier Function to be executed.
     * @return Result of supplier invocation.
     * @throws NullPointerException if class loader or supplier is null
     */
    @Beta
    public static <V> V getWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Supplier<V> supplier) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(requireNonNull(cls));
        try {
            return requireNonNull(supplier).get();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    /**
     * Immediately call {@link Runnable#run()} with provided {@link ClassLoader}. This method safely switches
     * the thread's Thread Context Class Loader to the specified class loader for the duration of execution of that
     * method.
     *
     * @param cls {@link ClassLoader} to be used.
     * @param runnable Function to be executed.
     * @throws NullPointerException if class loader or runnable is null
     */
    @Beta
    public static void runWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Runnable runnable) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldCls = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(requireNonNull(cls));
        try {
            requireNonNull(runnable).run();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
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
                final String outerName = DOT_JOINER.join(Iterables.limit(components, length));
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
        final Thread thread = Thread.currentThread();
        final ClassLoader tccl = thread.getContextClassLoader();
        if (tccl == null) {
            throw new ClassNotFoundException("Thread " + thread + " does not have a Context Class Loader, cannot load "
                    + name);
        }
        return loadClass(tccl, name);
    }

    // FIXME: 3.0.0: Document and return Optional
    public static Class<?> tryToLoadClassWithTCCL(final String fullyQualifiedClassName) {
        final Thread thread = Thread.currentThread();
        final ClassLoader tccl = thread.getContextClassLoader();
        if (tccl == null) {
            LOG.debug("Thread {} does not have a Context Class Loader, not loading class {}", thread,
                fullyQualifiedClassName);
            return null;
        }

        try {
            return loadClass(tccl, fullyQualifiedClassName);
        } catch (final ClassNotFoundException e) {
            LOG.debug("Failed to load class {}", fullyQualifiedClassName, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <S, G, P> Class<P> findFirstGenericArgument(final Class<S> scannedClass, final Class<G> genericType) {
        return getWithClassLoader(scannedClass.getClassLoader(), () -> {
            final ParameterizedType augmentationGeneric = findParameterizedType(scannedClass, genericType);
            return augmentationGeneric != null ? (Class<P>) augmentationGeneric.getActualTypeArguments()[0] : null;
        });
    }

    // FIXME: 3.0.0: Document and return Optional
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

    // FIXME: 3.0.0: Document and return Optional
    public static Type getFirstGenericParameter(final Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        return null;
    }
}
