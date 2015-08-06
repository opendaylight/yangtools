/*
 * Copyright (c) 2014 Robert Varga. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Convenience {@link ExceptionMapper} which instantiates specified Exception using
 * reflection. The Exception types are expected to declare an accessible constructor
 * which takes two arguments: a String and a Throwable.
 *
 * @param <X> Exception type
 */
public final class ReflectiveExceptionMapper<X extends Exception> extends ExceptionMapper<X> {
    private final Constructor<X> ctor;

    private ReflectiveExceptionMapper(final String opName, final Constructor<X> ctor) {
        super(opName, ctor.getDeclaringClass());
        this.ctor = ctor;
    }

    @Override
    protected X newWithCause(final String message, final Throwable cause) {
        try {
            return ctor.newInstance(message, cause);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate exception " + ctor.getDeclaringClass(), e);
        }
    }

    /**
     * Create a new instance of the reflective exception mapper. This method performs basic
     * sanity checking on the exception class. This method is potentially very costly, so
     * users are strongly encouraged to cache the returned mapper for reuse.
     *
     * @param opName Operation performed
     * @param exceptionType Exception type
     * @return A new mapper instance
     * @throws IllegalArgumentException when the supplied exception class does not pass sanity checks
     * @throws SecurityException when the required constructor is not accessible
     */
    public static <X extends Exception> ReflectiveExceptionMapper<X> create(final String opName, final Class<X> exceptionType) throws SecurityException {
        final Constructor<X> c;
        try {
            c = exceptionType.getConstructor(String.class, Throwable.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not define a String, Throwable constructor", e);
        }

        try {
            c.newInstance(opName, new Throwable());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException("Constructor " + c.getName() + " failed to pass instantiation test", e);
        }

        return new ReflectiveExceptionMapper<>(opName, c);
    }
}
