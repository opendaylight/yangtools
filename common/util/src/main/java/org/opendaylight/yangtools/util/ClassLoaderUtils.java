/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility methods for working with ClassLoaders and classes.
 */
public final class ClassLoaderUtils {
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
}
