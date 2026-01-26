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
 * Declared representation of a {@code error-app-tag} statement.
 */
public interface ErrorAppTagStatement extends DeclaredStatement<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ErrorAppTagStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ErrorAppTagStatement} or {@code null} if not present}
         */
        default @Nullable ErrorAppTagStatement errorAppTagStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ErrorAppTagStatement errorAppTag) {
                    return errorAppTag;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ErrorAppTagStatement}}
         */
        default @NonNull Optional<ErrorAppTagStatement> findErrorAppTagStatement() {
            return Optional.ofNullable(errorAppTagStatement());
        }

        /**
         * {@return the {@code ErrorAppTagStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ErrorAppTagStatement getErrorAppTagStatement() {
            final var errorAppTag = errorAppTagStatement();
            if (errorAppTag == null) {
                throw new NoSuchElementException("No error-app-tag statement present in " + this);
            }
            return errorAppTag;
        }
    }

    /**
     * The definition of {@code error-app-tag} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull ErrorAppTagStatement, @NonNull ErrorAppTagEffectiveStatement> DEF =
        StatementDefinition.of(ErrorAppTagStatement.class, ErrorAppTagEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "error-app-tag", "value");

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
