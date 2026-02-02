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
 * Declared representation of a {@code range} statement.
 */
public interface RangeStatement extends DescriptionStatement.OptionalIn<ValueRanges>,
        ErrorAppTagStatement.OptionalIn<ValueRanges>, ErrorMessageStatement.OptionalIn<ValueRanges>,
        ReferenceStatement.OptionalIn<ValueRanges> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link RangeStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code RangeStatement} or {@code null} if not present}
         */
        default @Nullable RangeStatement rangeStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof RangeStatement range) {
                    return range;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code RangeStatement}}
         */
        default @NonNull Optional<RangeStatement> findRangeStatement() {
            return Optional.ofNullable(rangeStatement());
        }

        /**
         * {@return the {@code RangeStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull RangeStatement getRangeStatement() {
            final var range = rangeStatement();
            if (range == null) {
                throw new NoSuchElementException("No range statement present in " + this);
            }
            return range;
        }
    }

    /**
     * The definition of {@code range} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<ValueRanges, @NonNull RangeStatement, @NonNull RangeEffectiveStatement> DEF =
        StatementDefinition.of(RangeStatement.class, RangeEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "range", YangArgumentDefinitions.VALUE_AS_VALUE_RANGES);

    @Override
    default StatementDefinition<ValueRanges, ?, ?> statementDefinition() {
        return DEF;
    }
}
