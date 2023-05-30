/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
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

    private ClassLoaderUtils() {
        // Hidden on purpose
    }

    /**
     * Immediately call {@link Function#apply(Object)} with provided {@link ClassLoader}. This method safely switches
     * the thread's Thread Context Class Loader to the specified class loader for the duration of execution of that
     * method.
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @param cls {@link ClassLoader} to be used.
     * @param function Function to be applied.
     * @param input Function input
     * @return Result of function invocation.
     * @throws NullPointerException if class loader or function is null
     */
    public static <T, R> R applyWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Function<T, R> function,
            final T input) {
        final var currentThread = Thread.currentThread();
        final var oldCls = currentThread.getContextClassLoader();
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
     * @param <V> the result type of the callable
     * @param cls {@link ClassLoader} to be used.
     * @param callable Function to be executed.
     * @return Result of callable invocation.
     * @throws NullPointerException if class loader or callable is null
     * @throws Exception if the callable fails
     */
    public static <V> V callWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Callable<V> callable)
            throws Exception {
        final var currentThread = Thread.currentThread();
        final var oldCls = currentThread.getContextClassLoader();
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
     * @param <V> the result type of the supplier
     * @param cls {@link ClassLoader} to be used.
     * @param supplier Function to be executed.
     * @return Result of supplier invocation.
     * @throws NullPointerException if class loader or supplier is null
     */
    public static <V> V getWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Supplier<V> supplier) {
        final var currentThread = Thread.currentThread();
        final var oldCls = currentThread.getContextClassLoader();
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
    public static void runWithClassLoader(final @NonNull ClassLoader cls, final @NonNull Runnable runnable) {
        final var currentThread = Thread.currentThread();
        final var oldCls = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(requireNonNull(cls));
        try {
            requireNonNull(runnable).run();
        } finally {
            currentThread.setContextClassLoader(oldCls);
        }
    }

    @SuppressWarnings("unchecked")
    public static <S, G, P> Optional<Class<P>> findFirstGenericArgument(final Class<S> scannedClass,
            final Class<G> genericType) {
        return getWithClassLoader(scannedClass.getClassLoader(),
            () -> findParameterizedType(scannedClass, genericType)
                .map(ptype -> (Class<P>) ptype.getActualTypeArguments()[0]));
    }

    /**
     * Find the parameterized instantiation of a particular interface implemented by a class.
     *
     * @param subclass Implementing class
     * @param genericType Interface to search for
     * @return Parameterized interface as implemented by the class, if present
     */
    public static Optional<ParameterizedType> findParameterizedType(final Class<?> subclass,
            final Class<?> genericType) {
        requireNonNull(genericType);

        for (var type : subclass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType ptype) {
                if (genericType.equals(ptype.getRawType())) {
                    return Optional.of(ptype);
                }
            }
        }

        LOG.debug("Class {} does not declare interface {}", subclass, genericType);
        return Optional.empty();
    }

    /**
     * Extract the first generic type argument for a Type. If the type is not parameterized, this method returns empty.
     *
     * @param type Type to examine
     * @return First generic type argument, if present
     * @throws NullPointerException if {@code type} is null
     */
    public static Optional<Type> getFirstGenericParameter(final Type type) {
        requireNonNull(type);
        return type instanceof ParameterizedType ptype ? Optional.of(ptype.getActualTypeArguments()[0])
            : Optional.empty();
    }
}
