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
 * Effective representation of a {@code description} statement.
 */
public interface DescriptionEffectiveStatement extends EffectiveHumanTextStatement<@NonNull DescriptionStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link DescriptionEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code DescriptionEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable DescriptionEffectiveStatement descriptionStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof DescriptionEffectiveStatement description) {
                    return description;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code DescriptionEffectiveStatement}}
         */
        default @NonNull Optional<DescriptionEffectiveStatement> findDescriptionStatement() {
            return Optional.ofNullable(descriptionStatement());
        }

        /**
         * {@return the {@code DescriptionEffectiveStatement}}
         *
         * @throws NoSuchElementException if not present
         */
        default @NonNull DescriptionEffectiveStatement getDescriptionStatement() {
            final var description = descriptionStatement();
            if (description == null) {
                throw new NoSuchElementException("No description statement present in " + this);
            }
            return description;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull DescriptionStatement, ?> statementDefinition() {
        return DescriptionStatement.DEF;
    }
}
