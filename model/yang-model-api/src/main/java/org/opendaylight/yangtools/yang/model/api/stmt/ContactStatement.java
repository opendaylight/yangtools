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
 * Declared representation of a {@code contact} statement.
 */
public interface ContactStatement extends DeclaredHumanTextStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ContactStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ContactStatement} or {@code null} if not present}
         */
        default @Nullable ContactStatement contactStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ContactStatement contact) {
                    return contact;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ContactStatement}}
         */
        default @NonNull Optional<ContactStatement> findContactStatement() {
            return Optional.ofNullable(contactStatement());
        }

        /**
         * {@return the {@code ContactStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ContactStatement getContactStatement() {
            final var contact = contactStatement();
            if (contact == null) {
                throw new NoSuchElementException("No contact statement present in " + this);
            }
            return contact;
        }
    }

    /**
     * The definition of {@code contact} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull ContactStatement, @NonNull ContactEffectiveStatement> DEF =
        StatementDefinition.of(ContactStatement.class, ContactEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "contact", "text", true);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
