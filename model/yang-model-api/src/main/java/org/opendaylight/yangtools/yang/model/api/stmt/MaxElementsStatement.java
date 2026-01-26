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
 * Declared representation of a {@code max-elements} statement.
 */
public interface MaxElementsStatement extends DeclaredStatement<MaxElementsArgument> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link MaxElementsStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code MaxElementsStatement} or {@code null} if not present}
         */
        default @Nullable MaxElementsStatement maxElementsStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof MaxElementsStatement maxElements) {
                    return maxElements;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code MaxElementsStatement}}
         */
        default @NonNull Optional<MaxElementsStatement> findMaxElementsStatement() {
            return Optional.ofNullable(maxElementsStatement());
        }

        /**
         * {@return the {@code MaxElementsStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull MaxElementsStatement getMaxElementsStatement() {
            final var maxElements = maxElementsStatement();
            if (maxElements == null) {
                throw new NoSuchElementException("No max-elements statement present in " + this);
            }
            return maxElements;
        }
    }

    /**
     * The definition of {@code max-elements} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<MaxElementsArgument, @NonNull MaxElementsStatement,
        @NonNull MaxElementsEffectiveStatement> DEF = StatementDefinition.of(
            MaxElementsStatement.class, MaxElementsEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "max-elements", "value");

    @Override
    default StatementDefinition<MaxElementsArgument, ?, ?> statementDefinition() {
        return DEF;
    }
}
