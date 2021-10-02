/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.eclipse.jdt.annotation.NonNull;
import org.hamcrest.Matcher;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Abstract base class containing useful utilities and assertions.
 */
public abstract class AbstractYangTest {
    public static @NonNull EffectiveModelContext assertEffectiveModel(final String... yangResourceName)
            throws Exception {
        final var ret = TestUtils.parseYangSource(yangResourceName);
        assertNotNull(ret);
        return ret;
    }

    public static @NonNull EffectiveModelContext assertEffectiveModelDir(final String resourceDirName)
            throws Exception {
        final var ret =  TestUtils.loadModules(resourceDirName);
        assertNotNull(ret);
        return ret;
    }

    public static <E extends SourceException> @NonNull E assertException(final Class<E> cause,
            final String... yangResourceName) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.parseYangSource(yangResourceName));
        final var actual = ex.getCause();
        assertThat(actual, instanceOf(cause));
        return cause.cast(actual);
    }

    public static <E extends SourceException> @NonNull E assertException(final Class<E> cause,
            final Matcher<String> matcher, final String... yangResourceName) {
        final var ret = assertException(cause, yangResourceName);
        assertThat(ret.getMessage(), matcher);
        return ret;
    }

    public static <E extends SourceException> @NonNull E assertExceptionDir(final String yangResourceName,
            final Class<E> cause) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules(yangResourceName));
        final var actual = ex.getCause();
        assertThat(actual, instanceOf(cause));
        return cause.cast(actual);
    }

    public static <E extends SourceException> @NonNull E assertExceptionDir(final String yangResourceName,
            final Class<E> cause, final Matcher<String> matcher) {
        final var ret = assertExceptionDir(yangResourceName, cause);
        assertThat(ret.getMessage(), matcher);
        return ret;
    }

    public static @NonNull InferenceException assertInferenceException(final Matcher<String> matcher,
            final String... yangResourceName) {
        return assertException(InferenceException.class, matcher, yangResourceName);
    }

    public static @NonNull InferenceException assertInferenceExceptionDir(final String yangResourceName,
            final Matcher<String> matcher) {
        return assertExceptionDir(yangResourceName, InferenceException.class, matcher);
    }

    public static @NonNull InvalidSubstatementException assertInvalidSubstatementException(
            final Matcher<String> matcher, final String... yangResourceName) {
        return assertException(InvalidSubstatementException.class, matcher, yangResourceName);
    }

    public static @NonNull InvalidSubstatementException assertInvalidSubstatementExceptionDir(
            final String yangResourceName, final Matcher<String> matcher) {
        return assertExceptionDir(yangResourceName, InvalidSubstatementException.class, matcher);
    }

    public static @NonNull SourceException assertSourceException(final Matcher<String> matcher,
            final String... yangResourceName) {
        final var ret = assertException(SourceException.class, matcher, yangResourceName);
        // SourceException is the base of the hierarchy, we should normally assert subclasses
        assertEquals(SourceException.class, ret.getClass());
        return ret;
    }

    public static @NonNull SourceException assertSourceExceptionDir(final String yangResourceName,
            final Matcher<String> matcher) {
        final var ret = assertExceptionDir(yangResourceName, SourceException.class, matcher);
        // SourceException is the base of the hierarchy, we should normally assert subclasses
        assertEquals(SourceException.class, ret.getClass());
        return ret;
    }
}
