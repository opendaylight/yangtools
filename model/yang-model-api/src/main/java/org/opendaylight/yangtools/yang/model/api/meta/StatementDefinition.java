/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;

/**
 * Definition / model of YANG {@link DeclaredStatement} and {@link EffectiveStatement}.
 *
 * <p>Statement concept is defined in RFC6020 section 6.3: <blockquote>A YANG module contains a sequence of statements.
 * Each statement starts with a keyword, followed by zero or one argument</blockquote>
 *
 * <p>Source: <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.3">RFC6020, section 6.3</a>
 */
// FIXME: a hidden record implementation
public sealed interface StatementDefinition extends Immutable permits DefaultStatementDefinition, YangStmtMapping {
    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final QName statementName, final Class<D> declaredType, final Class<E> effectiveType,
            final @Nullable ArgumentDefinition argument) {
        return new DefaultStatementDefinition<>(statementName, declaredType, effectiveType, argument);
    }

    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final QNameModule module, final String statementName, final Class<D> declaredType,
            final Class<E> effectiveType) {
        return of(QName.create(module, statementName).intern(), declaredType, effectiveType, null);
    }

    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final QNameModule module, final String statementName, final String argumentName, final boolean yinElement,
            final Class<D> declaredType, final Class<E> effectiveType) {
        return of(QName.create(module, statementName).intern(), declaredType, effectiveType,
            ArgumentDefinition.of(QName.create(module, argumentName).intern(), yinElement));
    }

    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition ofYang(
            final String statementName, final String argumentName, final Class<D> declaredType,
            final Class<E> effectiveType) {
        return of(YangConstants.RFC6020_YIN_MODULE, statementName, argumentName, false, declaredType, effectiveType);
    }

    /**
     * {@return name of the statement}
     */
    @NonNull QName statementName();

    /**
     * Returns name of the statement.
     *
     * @return Name of the statement
     * @deprecated Use {@link #statementName()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @NonNull QName getStatementName() {
        return statementName();
    }

    /**
     * {@return {@link ArgumentDefinition} or {@code null}, if statement does not take argument}
     */
    @Nullable ArgumentDefinition argumentDefinition();

    /**
     * {@return an optional {@link ArgumentDefinition}}
     */
    @SuppressWarnings("null")
    default @NonNull Optional<ArgumentDefinition> findArgumentDefinition() {
        return Optional.ofNullable(argumentDefinition());
    }

    /**
     * {@return {@link ArgumentDefinition}}
     * @throws NoSuchElementException if statement does not take argument
     */
    default @NonNull ArgumentDefinition getArgumentDefinition() {
        final var argDef = argumentDefinition();
        if (argDef == null) {
            throw new NoSuchElementException(statementName() + " does not take an argument");
        }
        return argDef;
    }

    /**
     * Returns class which represents declared version of statement associated with this definition. This class should
     * be an interface which provides convenience access to declared substatements.
     *
     * @return class which represents declared version of statement associated with this definition.
     */
    // FIXME: declaredRepresentation()
    @NonNull Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass();

    /**
     * Returns class which represents derived behaviour from supplied statement. This class should be an interface which
     * defines convenience access to statement properties, namespace items and substatements.
     *
     * @return class which represents effective version of statement associated with this definition
     */
    // FIXME: effectiveRepresentation()
    @NonNull Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass();
}
