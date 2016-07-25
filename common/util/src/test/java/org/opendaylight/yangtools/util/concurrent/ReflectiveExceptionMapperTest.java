/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import org.junit.Test;

public final class ReflectiveExceptionMapperTest {
    static final class NoArgumentCtorException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoArgumentCtorException() {
            super();
        }
    }

    static final class PrivateCtorException extends Exception {
        private static final long serialVersionUID = 1L;

        private PrivateCtorException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    static final class FailingCtorException extends Exception {
        private static final long serialVersionUID = 1L;

        public FailingCtorException(final String message, final Throwable cause) {
            throw new IllegalArgumentException("just for test");
        }
    }

    static final class GoodException extends Exception {
        private static final long serialVersionUID = 1L;

        public GoodException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }


    @Test(expected=IllegalArgumentException.class)
    public void testNoArgumentsContructor() {
        ReflectiveExceptionMapper.create("no arguments", NoArgumentCtorException.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testPrivateContructor() {
        ReflectiveExceptionMapper.create("private constructor", PrivateCtorException.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFailingContructor() {
        ReflectiveExceptionMapper.create("failing constructor", FailingCtorException.class);
    }

    @Test
    public void testInstantiation() {
        ReflectiveExceptionMapper<GoodException> mapper = ReflectiveExceptionMapper.create("instantiation", GoodException.class);

        final Throwable cause = new Throwable("some test message");

        GoodException ret = mapper.apply(new ExecutionException("test", cause));

        assertEquals("instantiation execution failed", ret.getMessage());
        assertEquals(cause, ret.getCause());
    }
}
