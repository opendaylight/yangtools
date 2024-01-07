/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * Common interface for all statement contexts, exposing information which is always available. Note this includes only
 * stateless information -- hence we have {@link #rawArgument()} but do not have an equivalent {@code argument()}.
 */
@NonNullByDefault
public interface CommonStmtCtx {
    /**
     * See {@link StatementSupport#getPublicView()}.
     */
    StatementDefinition publicDefinition();

    /**
     * Return true if this context produces specified {@link DeclaredStatement} representation.
     *
     * @param <D> Declared Statement representation
     * @param type DeclaredStatement representation
     * @return True if this context results in specified {@link DeclaredStatement} representation
     */
    default <D extends DeclaredStatement<?>> boolean producesDeclared(final Class<? super D> type) {
        return type.isAssignableFrom(publicDefinition().getDeclaredRepresentationClass());
    }

    /**
     * Return true if this context produces specified {@link EffectiveStatement} representation.
     *
     * @param <E> Effective Statement representation
     * @param type EffectiveStatement representation
     * @return True if this context results in specified {@link EffectiveStatement} representation
     */
    default <E extends EffectiveStatement<?, ?>> boolean producesEffective(final Class<? super E> type) {
        return type.isAssignableFrom(publicDefinition().getEffectiveRepresentationClass());
    }

    /**
     * Returns a reference to statement source.
     *
     * @return reference of statement source
     */
    StatementSourceReference sourceReference();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string, or null if this statement does not have an argument.
     */
    @Nullable String rawArgument();

    /**
     * Return the statement argument in literal format.
     *
     * @return raw statement argument string
     * @throws VerifyException if this statement does not have an argument
     */
    default String getRawArgument() {
        return verifyNotNull(rawArgument(), "Statement context %s does not have an argument", this);
    }

    /**
     * Infer that an expression is {@code false}. Throws an {@link InferenceException} if {@code expression} is
     * {@code true}.
     *
     * @param expression evaluated expression
     * @param format format string, according to {@link String#format(String, Object...)}.
     * @param args format string arguments, according to {@link String#format(String, Object...)}
     * @throws InferenceException if the expression evaluates to {@code true}.
     */
    default void inferFalse(final boolean expression, final String format, final Object... args) {
        if (expression) {
            throw newInferenceException(format, args);
        }
    }

    /**
     * Infer that an expression is {@code true}. Throws an {@link InferenceException} if {@code expression} is
     * {@code false}.
     *
     * @param expression evaluated expression
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @throws InferenceException if the expression evaluates to {@code false}.
     */
    default void inferTrue(final boolean expression, final String format, final Object... args) {
        if (!expression) {
            throw newInferenceException(format, args);
        }
    }

    /**
     * Infer that an object is not {@code null}.
     *
     * @param obj Object reference to be checked
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @return Object if not {@code null}
     * @throws InferenceException if {@code obj} is {@code null}
     */
    default <T> T inferNotNull(final @Nullable T obj, final String format, final Object... args) {
        if (obj == null) {
            throw newInferenceException(format, args);
        }
        return obj;
    }

    /**
     * Infer that an object is {@code null}.
     *
     * @param obj Object reference to be checked
     * @param format Format string, according to {@link String#format(String, Object...)}.
     * @param args Format string arguments, according to {@link String#format(String, Object...)}
     * @throws InferenceException if {@code obj} is not {@code null}
     */
    default void inferNull(final @Nullable Object obj, final String format, final Object... args) {
        if (obj != null) {
            throw newInferenceException(format, args);
        }
    }

    default InferenceException newInferenceException(final String message) {
        return new InferenceException(sourceReference(), message);
    }

    default InferenceException newInferenceException(final String message, final Throwable cause) {
        return new InferenceException(sourceReference(), message, cause);
    }

    default InferenceException newInferenceException(final String format, final Object... args) {
        return new InferenceException(sourceReference(), format, args);
    }
}
