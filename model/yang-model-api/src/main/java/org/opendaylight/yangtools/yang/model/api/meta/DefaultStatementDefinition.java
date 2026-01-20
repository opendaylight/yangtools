/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Default implementation of the {@link StatementDefinition} contract. Instances of this class should be used as
 * well-known singletons.
 *
 * @param <A> Argument type
 * @param <D> Declared statement representation
 * @param <E> Effective statement representation
 */
record DefaultStatementDefinition<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>(
        @NonNull QName statementName,
        @NonNull Class<D> declaredRepresentation,
        @NonNull Class<E> effectiveRepresentation,
        @Nullable ArgumentDefinition argumentDefinition) implements StatementDefinition {
    DefaultStatementDefinition {
        requireNonNull(statementName);
        if (!declaredRepresentation.isInterface()) {
            throw new IllegalArgumentException(
                "Declared representation " + declaredRepresentation + " is not an interface");
        }
        if (!effectiveRepresentation.isInterface()) {
            throw new IllegalArgumentException(
                "Effective representation " + effectiveRepresentation + " is not an interface");
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(StatementDefinition.class).add("name", statementName);
        final var argDef = argumentDefinition;
        if (argDef != null) {
            helper.add("argument", argDef.argumentName()).add("yin-element", argDef.isYinElement());
        }
        return helper
            .add("declared", declaredRepresentation.getName())
            .add("effective", effectiveRepresentation.getName())
            .toString();
    }
}
