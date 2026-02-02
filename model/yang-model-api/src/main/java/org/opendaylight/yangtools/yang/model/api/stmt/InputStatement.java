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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code input} statement.
 */
public interface InputStatement extends DataDefinitionStatement.MultipleIn<QName>, GroupingStatementMultipleIn<QName>,
        MustStatement.MultipleIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link InputStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code InputStatement} or {@code null} if not present}
         */
        default @Nullable InputStatement inputStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof InputStatement input) {
                    return input;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code InputStatement}}
         */
        default @NonNull Optional<InputStatement> findInputStatement() {
            return Optional.ofNullable(inputStatement());
        }

        /**
         * {@return the {@code InputStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull InputStatement getInputStatement() {
            final var input = inputStatement();
            if (input == null) {
                throw new NoSuchElementException("No input statement present in " + this);
            }
            return input;
        }
    }

    /**
     * The definition of {@code input} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull InputStatement, @NonNull InputEffectiveStatement> DEF =
        StatementDefinition.of(InputStatement.class, InputEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "input");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
