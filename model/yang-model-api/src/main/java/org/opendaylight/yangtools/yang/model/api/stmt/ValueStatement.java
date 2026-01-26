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
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code value} statement.
 */
public interface ValueStatement extends DeclaredStatement<Integer> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ValueStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ValueStatement} or {@code null} if not present}
         */
        default @Nullable ValueStatement valueStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ValueStatement value) {
                    return value;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ValueStatement}}
         */
        default @NonNull Optional<ValueStatement> findValueStatement() {
            return Optional.ofNullable(valueStatement());
        }

        /**
         * {@return the {@code ValueStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ValueStatement getValueStatement() {
            final var value = valueStatement();
            if (value == null) {
                throw new NoSuchElementException("No value statement present in " + this);
            }
            return value;
        }
    }

    /**
     * The definition of {@code value} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Integer, @NonNull ValueStatement, @NonNull ValueEffectiveStatement> DEF =
        StatementDefinition.of(ValueStatement.class, ValueEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "value", "value");

    @Override
    default StatementDefinition<Integer, ?, ?> statementDefinition() {
        return DEF;
    }
}
