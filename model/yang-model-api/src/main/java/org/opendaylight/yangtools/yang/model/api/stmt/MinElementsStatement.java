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
 * Declared representation of a {@code min-elements} statement.
 */
public interface MinElementsStatement extends DeclaredStatement<MinElementsArgument> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link MinElementsStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code MinElementsStatement} or {@code null} if not present}
         */
        default @Nullable MinElementsStatement minElementsStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof MinElementsStatement minElements) {
                    return minElements;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code MinElementsStatement}}
         */
        default @NonNull Optional<MinElementsStatement> findMinElementsStatement() {
            return Optional.ofNullable(minElementsStatement());
        }

        /**
         * {@return the {@code MinElementsStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull MinElementsStatement getMinElementsStatement() {
            final var minElements = minElementsStatement();
            if (minElements == null) {
                throw new NoSuchElementException("No min-elements statement present in " + this);
            }
            return minElements;
        }
    }

    /**
     * The definition of {@code min-elements} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<MinElementsArgument, @NonNull MinElementsStatement,
        @NonNull MinElementsEffectiveStatement> DEF = StatementDefinition.of(
            MinElementsStatement.class, MinElementsEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "min-elements", "value");

    @Override
    default StatementDefinition<MinElementsArgument, ?, ?> statementDefinition() {
        return DEF;
    }
}
