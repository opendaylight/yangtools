/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code ordered-by} statement.
 */
public interface OrderedByStatement extends DeclaredStatement<Ordering> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link OrderedByStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code OrderedByStatement} or {@code null} if not present}
         */
        default @Nullable OrderedByStatement orderedByStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof OrderedByStatement maxElements) {
                    return maxElements;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code OrderedByStatement}}
         */
        default @NonNull Optional<OrderedByStatement> findOrderedByStatement() {
            return Optional.ofNullable(orderedByStatement());
        }

        /**
         * {@return the {@code OrderedByStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull OrderedByStatement getOrderdByStatement() {
            final var orderedBy = orderedByStatement();
            if (orderedBy == null) {
                throw new NoSuchElementException("No ordered-by statement present in " + this);
            }
            return orderedBy;
        }
    }

    /**
     * The definition of {@code ordered-by} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Ordering, @NonNull OrderedByStatement, @NonNull OrderedByEffectiveStatement> DEF =
        StatementDefinition.of(OrderedByStatement.class, OrderedByEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "ordered-by",
            ArgumentDefinition.of(Ordering.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<Ordering, ?, ?> statementDefinition() {
        return DEF;
    }
}
