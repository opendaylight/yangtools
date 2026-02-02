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
 * Declared representation of a {@code output} statement.
 */
public interface OutputStatement extends DataDefinitionStatement.MultipleIn<QName>, GroupingStatementMultipleIn<QName>,
        MustStatement.MultipleIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link OutputStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code OutputStatement} or {@code null} if not present}
         */
        default @Nullable OutputStatement outputStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof OutputStatement output) {
                    return output;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code OutputStatement}}
         */
        default @NonNull Optional<OutputStatement> findOutputStatement() {
            return Optional.ofNullable(outputStatement());
        }

        /**
         * {@return the {@code OutputStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull OutputStatement getOutputStatement() {
            final var output = outputStatement();
            if (output == null) {
                throw new NoSuchElementException("No output statement present in " + this);
            }
            return output;
        }
    }

    /**
     * The definition of {@code output} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull OutputStatement, @NonNull OutputEffectiveStatement> DEF =
        StatementDefinition.of(OutputStatement.class, OutputEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "output");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
