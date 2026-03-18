/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code presence} statement.
 */
public interface PresenceEffectiveStatement extends EffectiveStatement<String, @NonNull PresenceStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link PresenceEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code PresenceEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable PresenceEffectiveStatement presenceStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof PresenceEffectiveStatement presence) {
                    return presence;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code PresenceEffectiveStatement}}
         */
        default @NonNull Optional<PresenceEffectiveStatement> findPresenceStatement() {
            return Optional.ofNullable(presenceStatement());
        }

        /**
         * {@return the {@code PresenceEffectiveStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull PresenceEffectiveStatement getPresenceByStatement() {
            final var presence = presenceStatement();
            if (presence == null) {
                throw new NoSuchElementException("No presence statement present in " + this);
            }
            return presence;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull PresenceStatement, ?> statementDefinition() {
        return PresenceStatement.DEF;
    }
}
