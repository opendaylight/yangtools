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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code error-message} statement.
 */
public interface ErrorMessageStatement extends DeclaredHumanTextStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ErrorMessageStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ErrorMessageStatement} or {@code null} if not present}
         */
        default @Nullable ErrorMessageStatement errorMessageStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ErrorMessageStatement errorMessage) {
                    return errorMessage;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ErrorMessageStatement}}
         */
        default @NonNull Optional<ErrorMessageStatement> findErrorMessageStatement() {
            return Optional.ofNullable(errorMessageStatement());
        }

        /**
         * {@return the {@code ErrorMessageStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ErrorMessageStatement getErrorMessageStatement() {
            final var errorMessage = errorMessageStatement();
            if (errorMessage == null) {
                throw new NoSuchElementException("No error-message statement present in " + this);
            }
            return errorMessage;
        }
    }

    /**
     * The definition of {@code error-message} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull ErrorMessageStatement, @NonNull ErrorMessageEffectiveStatement> DEF =
        StatementDefinition.of(ErrorMessageStatement.class, ErrorMessageEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "error-message", "value", true);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
