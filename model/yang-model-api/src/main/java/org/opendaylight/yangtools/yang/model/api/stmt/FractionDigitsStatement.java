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
 * Declared representation of a {@code fraction-digits} statement.
 */
public interface FractionDigitsStatement extends DeclaredStatement<Integer> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link FractionDigitsStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code FractionDigitsStatement} or {@code null} if not present}
         */
        default @Nullable FractionDigitsStatement fractionDigitsStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof FractionDigitsStatement fractionDigits) {
                    return fractionDigits;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code FractionDigitsStatement}}
         */
        default @NonNull Optional<FractionDigitsStatement> findFractionDigitsStatement() {
            return Optional.ofNullable(fractionDigitsStatement());
        }

        /**
         * {@return the {@code FractionDigitsStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull FractionDigitsStatement getConfigStatement() {
            final var fractionDigits = fractionDigitsStatement();
            if (fractionDigits == null) {
                throw new NoSuchElementException("No fraction-digits statement present in " + this);
            }
            return fractionDigits;
        }
    }


    /**
     * The definition of {@code fraction-digits} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Integer, @NonNull FractionDigitsStatement, @NonNull FractionDigitsEffectiveStatement>
        DEF = StatementDefinition.of(FractionDigitsStatement.class, FractionDigitsEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "fraction-digits", YangArgumentDefinitions.VALUE_AS_INTEGER);

    @Override
    default StatementDefinition<Integer, ?, ?> statementDefinition() {
        return DEF;
    }
}
