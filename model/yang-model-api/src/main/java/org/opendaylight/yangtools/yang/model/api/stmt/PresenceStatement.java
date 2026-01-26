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
 * Declared representation of a {@code presence} statement.
 */
public interface PresenceStatement extends DeclaredStatement<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link PresenceStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code PresenceStatement} or {@code null} if not present}
         */
        default @Nullable PresenceStatement presenceStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof PresenceStatement presence) {
                    return presence;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code PresenceStatement}}
         */
        default @NonNull Optional<PresenceStatement> findPresenceStatement() {
            return Optional.ofNullable(presenceStatement());
        }

        /**
         * {@return the {@code PresenceStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull PresenceStatement getPresenceStatement() {
            final var presence = presenceStatement();
            if (presence == null) {
                throw new NoSuchElementException("No presence statement present in " + this);
            }
            return presence;
        }
    }

    /**
     * The definition of {@code presence} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull PresenceStatement, @NonNull PresenceEffectiveStatement> DEF =
        StatementDefinition.of(PresenceStatement.class, PresenceEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "presence", YangArgumentDefinitions.VALUE_AS_STRING);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
