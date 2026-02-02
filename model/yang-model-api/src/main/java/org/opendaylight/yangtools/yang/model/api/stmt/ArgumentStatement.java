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
 * Declared representation of a {@code argument} statement.
 */
public interface ArgumentStatement extends YinElementStatement.OptionalIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ArgumentStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ArgumentStatement} or {@code null} if not present}
         */
        default @Nullable ArgumentStatement argumentStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ArgumentStatement argument) {
                    return argument;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ArgumentStatement}}
         */
        default @NonNull Optional<ArgumentStatement> findArgumentStatement() {
            return Optional.ofNullable(argumentStatement());
        }

        /**
         * {@return the {@code ArgumentStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ArgumentStatement getArgumentStatement() {
            final var argument = argumentStatement();
            if (argument == null) {
                throw new NoSuchElementException("No argument statement present in " + this);
            }
            return argument;
        }
    }

    /**
     * The definition of {@code argument} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ArgumentStatement, @NonNull ArgumentEffectiveStatement> DEF =
        StatementDefinition.of(ArgumentStatement.class, ArgumentEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "argument", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
