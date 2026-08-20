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
 * Effective representation of a {@code min-elements} statement.
 */
public interface MinElementsEffectiveStatement
        extends EffectiveStatement<MinElementsArgument, @NonNull MinElementsStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link MinElementsEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code MinElementsEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable MinElementsEffectiveStatement minElementsStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof MinElementsEffectiveStatement minElements) {
                    return minElements;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code MinElementsEffectiveStatement}}
         */
        default @NonNull Optional<MinElementsEffectiveStatement> findMinElementsStatement() {
            return Optional.ofNullable(minElementsStatement());
        }

        /**
         * {@return the {@code MinElementsEffectiveStatement}}
         *
         * @throws NoSuchElementException if not present
         */
        default @NonNull MinElementsEffectiveStatement getMinElementsStatement() {
            final var minElements = minElementsStatement();
            if (minElements == null) {
                throw new NoSuchElementException("No min-elements statement present in " + this);
            }
            return minElements;
        }
    }

    @Override
    default StatementDefinition<MinElementsArgument, @NonNull MinElementsStatement, ?> statementDefinition() {
        return MinElementsStatement.DEF;
    }
}
