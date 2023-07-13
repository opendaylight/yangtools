/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class ReflectiveExceptionMapperTest {
    public static final class NoArgumentCtorException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public NoArgumentCtorException() {
            // Noop
        }
    }

    public static final class PrivateCtorException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private PrivateCtorException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static final class FailingCtorException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public FailingCtorException(final String message, final Throwable cause) {
            throw new IllegalArgumentException("just for test");
        }
    }

    public static final class GoodException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public GoodException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }


    @Test
    void testNoArgumentsContructor() {
        assertThrows(IllegalArgumentException.class,
            () -> ReflectiveExceptionMapper.create("no arguments", NoArgumentCtorException.class));
    }

    @Test
    void testPrivateContructor() {
        assertThrows(IllegalArgumentException.class,
            () -> ReflectiveExceptionMapper.create("private constructor", PrivateCtorException.class));
    }

    @Test
    void testFailingContructor() {
        assertThrows(IllegalArgumentException.class,
            () -> ReflectiveExceptionMapper.create("failing constructor", FailingCtorException.class));
    }

    @Test
    void testInstantiation() {
        final var mapper = ReflectiveExceptionMapper.create("instantiation", GoodException.class);
        final var cause = new Throwable("some test message");
        final var ret = mapper.apply(new ExecutionException("test", cause));

        assertEquals("instantiation execution failed", ret.getMessage());
        assertEquals(cause, ret.getCause());
    }
}
