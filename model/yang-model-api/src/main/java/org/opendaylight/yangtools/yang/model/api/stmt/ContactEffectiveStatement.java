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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code contact} statement.
 */
public interface ContactEffectiveStatement extends EffectiveHumanTextStatement<@NonNull ContactStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link ContactEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code ContactEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable ContactEffectiveStatement contactStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof ContactEffectiveStatement contact) {
                    return contact;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ContactEffectiveStatement}}
         */
        default @NonNull Optional<ContactEffectiveStatement> findContactStatement() {
            return Optional.ofNullable(contactStatement());
        }

        /**
         * {@return the {@code ContactEffectiveStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ContactEffectiveStatement getContactStatement() {
            final var contact = contactStatement();
            if (contact == null) {
                throw new NoSuchElementException("No contact statement present in " + this);
            }
            return contact;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull ContactStatement, ?> statementDefinition() {
        return ContactStatement.DEF;
    }
}
