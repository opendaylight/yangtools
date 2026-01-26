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
 * Declared representation of a {@code require-instance} statement.
 */
public interface RequireInstanceStatement extends DeclaredStatement<Boolean> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link RequireInstanceStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code RequireInstanceStatement} or {@code null} if not present}
         */
        default @Nullable RequireInstanceStatement requireInstanceStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof RequireInstanceStatement requireInstance) {
                    return requireInstance;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code RequireInstanceStatement}}
         */
        default @NonNull Optional<RequireInstanceStatement> findRequireInstanceStatement() {
            return Optional.ofNullable(requireInstanceStatement());
        }

        /**
         * {@return the {@code RequireInstanceStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull RequireInstanceStatement getRequireInstanceStatement() {
            final var requireInstance = requireInstanceStatement();
            if (requireInstance == null) {
                throw new NoSuchElementException("No require-instance statement present in " + this);
            }
            return requireInstance;
        }
    }

    /**
     * The definition of {@code require-instance} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Boolean, @NonNull RequireInstanceStatement, @NonNull RequireInstanceEffectiveStatement>
        DEF = StatementDefinition.of(RequireInstanceStatement.class, RequireInstanceEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "require-instance", YangArgumentDefinitions.VALUE_AS_BOOLEAN);

    @Override
    default StatementDefinition<Boolean, ?, ?> statementDefinition() {
        return DEF;
    }
}
