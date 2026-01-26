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
 * Declared representation of a {@code reference} statement.
 */
public interface ReferenceStatement extends DeclaredHumanTextStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ReferenceStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ReferenceStatement} or {@code null} if not present}
         */
        default @Nullable ReferenceStatement referenceStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ReferenceStatement reference) {
                    return reference;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ReferenceStatement}}
         */
        default @NonNull Optional<ReferenceStatement> findReferenceStatement() {
            return Optional.ofNullable(referenceStatement());
        }

        /**
         * {@return the {@code ReferenceStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ReferenceStatement getReferenceStatement() {
            final var reference = referenceStatement();
            if (reference == null) {
                throw new NoSuchElementException("No status statement present in " + this);
            }
            return reference;
        }
    }

    /**
     * The definition of {@code reference} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull ReferenceStatement, @NonNull ReferenceEffectiveStatement> DEF =
        StatementDefinition.of(ReferenceStatement.class, ReferenceEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "reference", YangArgumentDefinitions.TEXT_AS_STRING);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
