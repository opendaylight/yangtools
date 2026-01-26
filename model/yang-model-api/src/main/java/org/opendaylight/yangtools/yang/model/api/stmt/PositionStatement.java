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
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code position} statement.
 */
public interface PositionStatement extends DeclaredStatement<Uint32> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link PositionStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code PositionStatement} or {@code null} if not present}
         */
        default @Nullable PositionStatement positionStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof PositionStatement position) {
                    return position;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code PositionStatement}}
         */
        default @NonNull Optional<PositionStatement> findPositionStatement() {
            return Optional.ofNullable(positionStatement());
        }

        /**
         * {@return the {@code PositionStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull PositionStatement getPositionStatement() {
            final var position = positionStatement();
            if (position == null) {
                throw new NoSuchElementException("No position statement present in " + this);
            }
            return position;
        }
    }


    /**
     * The definition of {@code position} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Uint32, @NonNull PositionStatement, @NonNull PositionEffectiveStatement> DEF =
        StatementDefinition.of(PositionStatement.class, PositionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "position", "value");

    @Override
    default StatementDefinition<Uint32, ?, ?> statementDefinition() {
        return DEF;
    }
}
