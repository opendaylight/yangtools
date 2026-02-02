/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code ordered-by} statement.
 */
public interface OrderedByEffectiveStatement extends EffectiveStatement<Ordering, @NonNull OrderedByStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link OrderedByEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.0
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code OrderedByEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable OrderedByEffectiveStatement orderedByStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof OrderedByEffectiveStatement orderedBy) {
                    return orderedBy;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code OrderedByEffectiveStatement}}
         */
        default @NonNull Optional<OrderedByEffectiveStatement> findOrderedByStatement() {
            return Optional.ofNullable(orderedByStatement());
        }

        /**
         * {@return the {@code OrderedByEffectiveStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull OrderedByEffectiveStatement getOrderedByStatement() {
            final var orderedBy = orderedByStatement();
            if (orderedBy == null) {
                throw new NoSuchElementException("No ordered-by statement present in " + this);
            }
            return orderedBy;
        }

        /**
         * {@return the effective {@link Ordering} of this statement}
         */
        default @NonNull Ordering effectiveOrdering() {
            final var orderedBy = orderedByStatement();
            return orderedBy != null ? orderedBy.argument() : Ordering.SYSTEM;
        }
    }

    @Override
    default StatementDefinition<Ordering, @NonNull OrderedByStatement, ?> statementDefinition() {
        return OrderedByStatement.DEF;
    }
}
