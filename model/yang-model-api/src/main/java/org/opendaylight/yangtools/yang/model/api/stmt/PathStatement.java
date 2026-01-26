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
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code path} statement.
 */
public interface PathStatement extends DeclaredStatement<PathExpression> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link PathStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code PathStatement} or {@code null} if not present}
         */
        default @Nullable PathStatement pathStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof PathStatement path) {
                    return path;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code PathStatement}}
         */
        default @NonNull Optional<PathStatement> findPathStatement() {
            return Optional.ofNullable(pathStatement());
        }

        /**
         * {@return the {@code PathStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull PathStatement getPathStatement() {
            final var path = pathStatement();
            if (path == null) {
                throw new NoSuchElementException("No path statement present in " + this);
            }
            return path;
        }
    }

    /**
     * The definition of {@code path} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<PathExpression, @NonNull PathStatement, @NonNull PathEffectiveStatement> DEF =
        StatementDefinition.of(PathStatement.class, PathEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "path", "value");

    @Override
    default StatementDefinition<PathExpression, ?, ?> statementDefinition() {
        return DEF;
    }
}
