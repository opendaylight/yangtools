/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Definition / model of YANG {@link DeclaredStatement} and {@link EffectiveStatement}.
 *
 * <p>Statement concept is defined in RFC6020 section 6.3: <blockquote>A YANG module contains a sequence of statements.
 * Each statement starts with a keyword, followed by zero or one argument</blockquote>
 *
 * <p>Source: <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.3">RFC6020, section 6.3</a>
 */
public sealed interface StatementDefinition<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends Immutable permits DefaultStatementDefinition {
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
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        @NonNull StatementDefinition<A, D, E> of(final @NonNull Class<? extends D> declaredRepresentation,
            final @NonNull Class<? extends E> effectiveRepresentation, final @NonNull QName statementName,
            final @Nullable ArgumentDefinition<A> argument) {
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
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> StatementDefinition<A, D, E> of(
            final Class<? extends D> declaredRepresentation, final Class<? extends E> effectiveRepresentation,
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
     * @param argument the {@link ArgumentDefinition}
     * @return a {@link StatementDefinition}
     */
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        @NonNull StatementDefinition<A, D, E> of(final @NonNull Class<? extends D> declaredRepresentation,
            final @NonNull Class<? extends E> effectiveRepresentation, final @NonNull QNameModule module,
            final @NonNull String statementName, final @NonNull ArgumentDefinition<@NonNull A> argument) {
        return of(declaredRepresentation, effectiveRepresentation, QName.create(module, statementName).intern(),
            requireNonNull(argument));
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
        return YangConstants.RFC6020_YIN_MODULE.equals(statementName.getModule()) ? statementName.getLocalName()
            : statementName.toString();
    }

    /**
     * {@return a plain statement name}
     * @since 15.0.0
     */
    default @NonNull String simpleName() {
        return statementName().getLocalName();
    }

    /**
     * {@return {@link ArgumentDefinition} or {@code null}, if statement does not take argument}
     */
    @Nullable ArgumentDefinition<A> argumentDefinition();

    /**
     * {@return an optional {@link ArgumentDefinition}}
     */
    @SuppressWarnings("null")
    default @NonNull Optional<ArgumentDefinition<A>> findArgumentDefinition() {
        return Optional.ofNullable(argumentDefinition());
    }

    /**
     * {@return {@link ArgumentDefinition}}
     * @throws NoSuchElementException if statement does not take argument
     */
    default @NonNull ArgumentDefinition<A> getArgumentDefinition() {
        final var argDef = argumentDefinition();
        if (argDef == null) {
            throw new NoSuchElementException(humanName() + " does not take an argument");
        }
        return argDef;
    }

    /**
     * {@return the class representing the declared version of the statement associated with this definition}
     */
    @NonNull Class<? extends D> declaredRepresentation();

    /**
     * {@return the class representing the effective version of the statement associated with this definition}
     */
    @NonNull Class<? extends E> effectiveRepresentation();

    /**
     * Returns class which represents declared version of statement associated with this definition. This class should
     * be an interface which provides convenience access to declared substatements.
     *
     * @return class which represents declared version of statement associated with this definition.
     * @deprecated Use {@link #declaredRepresentation()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default @NonNull Class<? extends D> getDeclaredRepresentationClass() {
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
    default @NonNull Class<? extends E> getEffectiveRepresentationClass() {
        return effectiveRepresentation();
    }

    /**
     * {@return {@code true} if this definition represents an {@code extension} statement}
     */
    default boolean isExtension() {
        return !YangConstants.RFC6020_YIN_MODULE.equals(statementName().getModule());
    }

    /**
     * {@return the {@link System#identityHashCode(Object)} of this object}
     */
    @Override
    int hashCode();

    /**
     * {@return {@code true} IFF {@code obj} is the same instance as this object}
     * @param obj the reference object with which to compare.
     */
    @Override
    boolean equals(Object obj);
}
