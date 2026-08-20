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
 * Effective representation of a {@code units} statement.
 */
public interface UnitsEffectiveStatement extends EffectiveStatement<String, @NonNull UnitsStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link UnitsEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code UnitsEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable UnitsEffectiveStatement unitsStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof UnitsEffectiveStatement units) {
                    return units;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code UnitsEffectiveStatement}}
         */
        default @NonNull Optional<UnitsEffectiveStatement> findUnitsStatement() {
            return Optional.ofNullable(unitsStatement());
        }

        /**
         * {@return the {@code UnitsEffectiveStatement}}
         *
         * @throws NoSuchElementException if not present
         */
        default @NonNull UnitsEffectiveStatement getUnitsStatement() {
            final var units = unitsStatement();
            if (units == null) {
                throw new NoSuchElementException("No units statement present in " + this);
            }
            return units;
        }
    }

    @Override
    default StatementDefinition<String, @NonNull UnitsStatement, ?> statementDefinition() {
        return UnitsStatement.DEF;
    }
}
