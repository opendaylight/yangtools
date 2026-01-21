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
public sealed interface StatementDefinition extends Immutable permits DefaultStatementDefinition, YangStmtMapping {
    /**
     * Return a {@link StatementDefinition} combining all specified components.
     *
     * @param <A> argument type
     * @param <D> declared statement representation
     * @param <E> effective statement representation
     * @param declaredRepresentation declared statement representation class
     * @param effectiveRepresentation effective statement representation class
     * @param statementName statement name
     * @param argument optional {@link ArgumentDefinition}
     * @return a {@link StatementDefinition}
     */
    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final Class<D> declaredRepresentation, final Class<E> effectiveRepresentation,
            final QName statementName, final @Nullable ArgumentDefinition argument) {
        return new DefaultStatementDefinition<>(statementName, declaredRepresentation, effectiveRepresentation,
            argument);
    }

    /**
     * Convenience method for creating a {@link StatementDefinition} without an argument. The statement name is
     * specified as a combination of a {@link QNameModule} and a {@link String}, which this method combines.
     *
     * @param <A> argument type
     * @param <D> declared statement representation
     * @param <E> effective statement representation
     * @param declaredRepresentation declared statement representation class
     * @param effectiveRepresentation effective statement representation class
     * @param module the module defining this statement
     * @param statementName statement name
     * @return a {@link StatementDefinition}
     */
    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final Class<D> declaredRepresentation, final Class<E> effectiveRepresentation,
            final QNameModule module, final String statementName) {
        return of(declaredRepresentation, effectiveRepresentation, QName.create(module, statementName).intern(), null);
    }

    /**
     * Convenience method for creating a {@link StatementDefinition} with an argument. Both statement name and argument
     * name are specified as a combination of a {@link QNameModule} and a {@link String}, which this method combines.
     *
     * @param <A> argument type
     * @param <D> declared statement representation
     * @param <E> effective statement representation
     * @param declaredRepresentation declared statement representation class
     * @param effectiveRepresentation effective statement representation class
     * @param module the module defining this statement
     * @param statementName statement name
     * @param argumentName statement name
     * @return a {@link StatementDefinition}
     */
    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final Class<D> declaredRepresentation, final Class<E> effectiveRepresentation,
            final QNameModule module, final String statementName,
            final String argumentName) {
        return of(declaredRepresentation, effectiveRepresentation, module, statementName, argumentName, false);
    }

    /**
     * Convenience method for creating a {@link StatementDefinition} with an argument. Both statement name and argument
     * name are specified as a combination of a {@link QNameModule} and a {@link String}, which this method combines.
     *
     * @param <A> argument type
     * @param <D> declared statement representation
     * @param <E> effective statement representation
     * @param declaredRepresentation declared statement representation class
     * @param effectiveRepresentation effective statement representation class
     * @param module the module defining this statement
     * @param statementName statement name
     * @param argumentName statement name
     * @param yinElement P@code true} if the argument is encoded as YIN element
     * @return a {@link StatementDefinition}
     */
    @NonNullByDefault
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition of(
            final Class<D> declaredRepresentation, final Class<E> effectiveRepresentation,
            final QNameModule module, final String statementName, final String argumentName, final boolean yinElement) {
        return of(declaredRepresentation, effectiveRepresentation, QName.create(module, statementName).intern(),
            ArgumentDefinition.of(QName.create(module, argumentName).intern(), yinElement));
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
     * {@return a human-friendly string representation of {link #statementName()}}
     * @since 15.0.0
     */
    default @NonNull String humanName() {
        final var statementName = statementName();
        return YangConstants.RFC6020_YIN_MODULE.equals(statementName) ? statementName.getLocalName()
            : statementName.toString();
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
     * {@return the class representing the declared version of the statement associated with this definition}
     */
    @NonNull Class<? extends DeclaredStatement<?>> declaredRepresentation();

    /**
     * {@return the class representing the effective version of the statement associated with this definition}
     */
    @NonNull Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation();

    /**
     * Returns class which represents declared version of statement associated with this definition. This class should
     * be an interface which provides convenience access to declared substatements.
     *
     * @return class which represents declared version of statement associated with this definition.
     * @deprecated Use {@link #declaredRepresentation()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @NonNull Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation();
    }

    /**
     * Returns class which represents derived behaviour from supplied statement. This class should be an interface which
     * defines convenience access to statement properties, namespace items and substatements.
     *
     * @return class which represents effective version of statement associated with this definition
     * @deprecated Use {@link #effectiveRepresentation()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @NonNull Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation();
    }
}
