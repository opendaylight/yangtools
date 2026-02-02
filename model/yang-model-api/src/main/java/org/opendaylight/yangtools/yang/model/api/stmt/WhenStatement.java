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
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Declared representation of a {@code when} statement.
 */
public interface WhenStatement extends DescriptionStatement.OptionalIn<QualifiedBound>,
        ReferenceStatement.OptionalIn<QualifiedBound> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link WhenStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code WhenStatement} or {@code null} if not present}
         */
        default @Nullable WhenStatement whenStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof WhenStatement when) {
                    return when;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code WhenStatement}}
         */
        default @NonNull Optional<WhenStatement> findWhenStatement() {
            return Optional.ofNullable(whenStatement());
        }

        /**
         * {@return the {@code WhenStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull WhenStatement getWhenStatement() {
            final var when = whenStatement();
            if (when == null) {
                throw new NoSuchElementException("No when statement present in " + this);
            }
            return when;
        }
    }

    /**
     * The definition of {@code when} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QualifiedBound, @NonNull WhenStatement, @NonNull WhenEffectiveStatement> DEF =
        StatementDefinition.of(WhenStatement.class, WhenEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "when", YangArgumentDefinitions.CONDITION_AS_QUALIFIED_BOUND);

    @Override
    default StatementDefinition<QualifiedBound, ?, ?> statementDefinition() {
        return DEF;
    }
}
