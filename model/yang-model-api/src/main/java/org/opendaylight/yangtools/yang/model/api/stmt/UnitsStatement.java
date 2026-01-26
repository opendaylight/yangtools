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
 * Declared representation of a {@code units} statement.
 */
public interface UnitsStatement extends DeclaredStatement<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link UnitsStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code UnitsStatement} or {@code null} if not present}
         */
        default @Nullable UnitsStatement unitsStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof UnitsStatement config) {
                    return config;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code UnitsStatement}}
         */
        default @NonNull Optional<UnitsStatement> findUnitsStatement() {
            return Optional.ofNullable(unitsStatement());
        }

        /**
         * {@return the {@code UnitsStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull UnitsStatement getUnitsStatement() {
            final var units = unitsStatement();
            if (units == null) {
                throw new NoSuchElementException("No units statement present in " + this);
            }
            return units;
        }
    }

    /**
     * The definition of {@code units} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull UnitsStatement, @NonNull UnitsEffectiveStatement> DEF =
        StatementDefinition.of(UnitsStatement.class, UnitsEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "units", YangArgumentDefinitions.NAME_AS_STRING);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
