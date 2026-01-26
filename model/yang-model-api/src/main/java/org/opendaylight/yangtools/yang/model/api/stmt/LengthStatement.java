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
 * Declared representation of a {@code length} statement.
 */
public interface LengthStatement extends DocumentedDeclaredStatement<ValueRanges>,
        ErrorAppTagStatement.OptionalIn<ValueRanges>, ErrorMessageStatement.OptionalIn<ValueRanges> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link LengthStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code LengthStatement} or {@code null} if not present}
         */
        default @Nullable LengthStatement lengthStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof LengthStatement length) {
                    return length;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code LengthStatement}}
         */
        default @NonNull Optional<LengthStatement> findLengthStatement() {
            return Optional.ofNullable(lengthStatement());
        }

        /**
         * {@return the {@code LengthStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull LengthStatement getLengthStatement() {
            final var length = lengthStatement();
            if (length == null) {
                throw new NoSuchElementException("No length statement present in " + this);
            }
            return length;
        }
    }

    /**
     * The definition of {@code length} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<ValueRanges, @NonNull LengthStatement, @NonNull LengthEffectiveStatement> DEF =
        StatementDefinition.of(LengthStatement.class, LengthEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "length", "value");

    @Override
    default StatementDefinition<ValueRanges, ?, ?> statementDefinition() {
        return DEF;
    }
}
