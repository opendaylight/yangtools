/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Matcher;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidBitDefinitionException;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidEnumDefinitionException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.InvalidSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Abstract base class containing useful utilities and assertions.
 */
public abstract class AbstractYangTest {
    public static @NonNull EffectiveModelContext assertEffectiveModel(final String... yangResourceName) {
        return assertEffectiveModel(List.of(yangResourceName), null);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public static @NonNull EffectiveModelContext assertEffectiveModel(final List<String> yangResourceName,
        final @Nullable Set<QName> supportedFeatures) {
        final EffectiveModelContext ret;
        try {
            ret = TestUtils.parseYangSource(yangResourceName, supportedFeatures);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new AssertionError("Failed to assemble effective model", e);
        }
        assertNotNull(ret);
        return ret;
    }

    public static @NonNull EffectiveModelContext assertEffectiveModelDir(final String resourceDirName) {
        return assertEffectiveModelDir(resourceDirName, null);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public static @NonNull EffectiveModelContext assertEffectiveModelDir(final String resourceDirName,
            final @Nullable Set<QName> supportedFeatures) {
        final EffectiveModelContext ret;
        try {
            ret = TestUtils.loadModules(resourceDirName, supportedFeatures);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new AssertionError("Failed to assemble effective model of " + resourceDirName, e);
        }
        assertNotNull(ret);
        return ret;
    }

    public static <E extends RuntimeException> @NonNull E assertException(final Class<E> cause,
            final String... yangResourceName) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.parseYangSource(yangResourceName));
        final var actual = ex.getCause();
        return assertInstanceOf(cause, actual);
    }

    public static <E extends StatementSourceException> @NonNull E assertException(final Class<E> cause,
            final Matcher<String> matcher, final String... yangResourceName) {
        final var ret = assertException(cause, yangResourceName);
        assertThat(ret.getMessage(), matcher);
        return ret;
    }

    public static <E extends IllegalArgumentException> @NonNull E assertArgumentException(final Class<E> cause,
            final Matcher<String> matcher, final String... yangResourceName) {
        final var ret = assertException(cause, yangResourceName);
        assertThat(ret.getMessage(), matcher);
        return ret;
    }

    public static <E extends StatementSourceException> @NonNull E assertExceptionDir(final String yangResourceName,
            final Class<E> cause) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.loadModules(yangResourceName));
        final var actual = ex.getCause();
        return assertInstanceOf(cause, actual);
    }

    public static <E extends StatementSourceException> @NonNull E assertExceptionDir(final String yangResourceName,
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

    public static @NonNull InvalidEnumDefinitionException assertInvalidEnumDefinitionException(
            final Matcher<String> matcher, final String... yangResourceName) {
        return assertArgumentException(InvalidEnumDefinitionException.class, matcher, yangResourceName);
    }

    public static @NonNull InvalidBitDefinitionException assertInvalidBitDefinitionException(
            final Matcher<String> matcher, final String... yangResourceName) {
        return assertArgumentException(InvalidBitDefinitionException.class, matcher, yangResourceName);
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
