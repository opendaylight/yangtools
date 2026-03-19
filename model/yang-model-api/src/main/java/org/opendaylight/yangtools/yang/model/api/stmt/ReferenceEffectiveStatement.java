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
 * Effective representation of a {@code reference} statement.
 */
public interface ReferenceEffectiveStatement extends EffectiveHumanTextStatement<@NonNull ReferenceStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link ReferenceEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code ReferenceEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable ReferenceEffectiveStatement referenceStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof ReferenceEffectiveStatement reference) {
                    return reference;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ReferenceEffectiveStatement}}
         */
        default @NonNull Optional<ReferenceEffectiveStatement> findReferenceStatement() {
            return Optional.ofNullable(referenceStatement());
        }

        /**
         * {@return the {@code ReferenceEffectiveStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ReferenceEffectiveStatement getReferenceStatement() {
            final var reference = referenceStatement();
            if (reference == null) {
                throw new NoSuchElementException("No reference statement present in " + this);
            }
            return reference;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull ReferenceStatement, ?> statementDefinition() {
        return ReferenceStatement.DEF;
    }
}
