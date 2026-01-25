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
 * Declared representation of a {@code mandatory} statement.
 */
public interface MandatoryStatement extends DeclaredStatement<Boolean> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link MandatoryStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code MandatoryStatement} or {@code null} if not present}
         */
        default @Nullable MandatoryStatement mandatoryStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof MandatoryStatement mandatory) {
                    return mandatory;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code MandatoryStatement}}
         */
        default @NonNull Optional<MandatoryStatement> findMandatoryStatement() {
            return Optional.ofNullable(mandatoryStatement());
        }

        /**
         * {@return the {@code MandatoryStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull MandatoryStatement getMandatoryStatement() {
            final var mandatory = mandatoryStatement();
            if (mandatory == null) {
                throw new NoSuchElementException("No mandatory statement present in " + this);
            }
            return mandatory;
        }
    }

    /**
     * The definition of {@code mandatory} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Boolean, @NonNull MandatoryStatement, @NonNull MandatoryEffectiveStatement> DEF =
        StatementDefinition.of(MandatoryStatement.class, MandatoryEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "mandatory", "value");

    @Override
    default StatementDefinition<Boolean, ?, ?> statementDefinition() {
        return DEF;
    }
}
