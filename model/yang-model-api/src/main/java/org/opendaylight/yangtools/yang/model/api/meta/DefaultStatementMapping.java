/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;

record DefaultStatementMapping<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>(
        @NonNull Class<A> argumentRepresentation,
        @NonNull Class<D> declaredRepresentation,
        @NonNull Class<E> effectiveRepresentation) implements StatementMapping<A, D, E> {
    DefaultStatementMapping {
        requireNonNull(argumentRepresentation);
        checkArgument(declaredRepresentation, effectiveRepresentation);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StatementMapping.class)
            .add("argument", argumentName())
            .add("declared", declaredName())
            .add("effective", effectiveName())
            .toString();
    }

    // exposed for DefaultStatementDefinition
    static void checkArgument(final @NonNull Class<? extends DeclaredStatement<?>> declared,
            final @NonNull Class<? extends EffectiveStatement<?, ?>> effective) {
        // FIXME: more reflections
        checkInstanceImplementing("Declared representation", DeclaredStatement.class, declared);
        checkInstanceImplementing("Effective representation", EffectiveStatement.class, effective);
    }

    private static void checkInstanceImplementing(final String what, final Class<?> expected,
            final Class<?> actual) {
        if (!actual.isInterface()) {
            throw new IllegalArgumentException(what + " is not an interface: " + actual);
        }
        if (!expected.isAssignableFrom(actual)) {
            throw new IllegalArgumentException(what + " does not implement " + expected.getName() + ": " + actual);
        }
    }

}
