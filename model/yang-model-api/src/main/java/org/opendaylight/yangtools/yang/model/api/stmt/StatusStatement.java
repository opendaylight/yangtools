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
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code status} statement.
 */
public interface StatusStatement extends DeclaredStatement<Status> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link StatusStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code StatusStatement} or {@code null} if not present}
         */
        default @Nullable StatusStatement statusStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof StatusStatement status) {
                    return status;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code StatusStatement}}
         */
        default @NonNull Optional<StatusStatement> findStatusStatement() {
            return Optional.ofNullable(statusStatement());
        }

        /**
         * {@return the {@code StatusStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull StatusStatement getStatusStatement() {
            final var status = statusStatement();
            if (status == null) {
                throw new NoSuchElementException("No status statement present in " + this);
            }
            return status;
        }
    }

    /**
     * The definition of {@code status} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Status, @NonNull StatusStatement, @NonNull StatusEffectiveStatement> DEF =
        StatementDefinition.of(StatusStatement.class, StatusEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "status",
            ArgumentDefinition.of(Status.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<Status, ?, ?> statementDefinition() {
        return DEF;
    }
}
