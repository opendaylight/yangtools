/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Default implementation of the {@link StatementDefinition} contract. Instances of this class should be used as
 * well-known singletons.
 *
 * @author Robert Varga
 *
 * @param <A> Argument type
 * @param <D> Declared statement representation
 * @param <E> Effective statement representation
 */
@Beta
@NonNullByDefault
public final class DefaultStatementDefinition<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractStatementDefinition {
    private final Class<E> effectiveRepresentation;
    private final Class<D> declaredRepresentation;

    DefaultStatementDefinition(final QName statementName, final Class<D> declaredRepresentation,
            final Class<E> effectiveRepresentation, final boolean argumentYinElement,
            final @Nullable QName argumentName) {
        super(statementName, argumentYinElement, argumentName);
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);

        checkArgument(declaredRepresentation.isInterface(), "Declared representation %s is not an interface",
            declaredRepresentation);
        checkArgument(effectiveRepresentation.isInterface(), "Effective representation %s is not an interface",
            effectiveRepresentation);
    }

    public static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            DefaultStatementDefinition<A, D, E> of(final QName statementName, final Class<D> declaredRepresentation,
                    final Class<E> effectiveRepresentation) {
        return new DefaultStatementDefinition<>(statementName, declaredRepresentation, effectiveRepresentation, false,
                null);
    }

    public static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            DefaultStatementDefinition<A, D, E> of(final QName statementName, final Class<D> declaredRepresentation,
                    final Class<E> effectiveRepresentation, final QName argumentName) {
        return of(statementName, declaredRepresentation, effectiveRepresentation, argumentName, false);
    }

    public static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            DefaultStatementDefinition<A, D, E> of(final QName statementName, final Class<D> declaredRepresentation,
                    final Class<E> effectiveRepresentation, final QName argumentName,
                    final boolean argumentYinElement) {
        return new DefaultStatementDefinition<>(statementName, declaredRepresentation, effectiveRepresentation,
                argumentYinElement, requireNonNull(argumentName));
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper)
                .add("declared", declaredRepresentation)
                .add("effective", effectiveRepresentation);
    }
}
