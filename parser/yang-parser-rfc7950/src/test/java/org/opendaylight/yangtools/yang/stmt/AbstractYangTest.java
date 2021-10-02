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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.eclipse.jdt.annotation.NonNull;
import org.hamcrest.Matcher;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
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

    public static @NonNull InferenceException assertInferenceException(final Matcher<String> matcher,
            final String... yangResourceName) {
        return assertException(InferenceException.class, matcher, yangResourceName);
    }

    public static @NonNull SourceException assertSourceException(final Matcher<String> matcher,
            final String... yangResourceName) {
        return assertException(SourceException.class, matcher, yangResourceName);
    }

    @Deprecated(forRemoval = true)
    public static void assertWrongException(final Matcher<String> matcher, final String... yangResourceName) {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> TestUtils.parseYangSource(yangResourceName));
        assertThat(ex.getCause(), instanceOf(IllegalArgumentException.class));
    }
}
