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
 * Declared representation of a {@code config} statement.
 */
public interface ConfigStatement extends DeclaredStatement<Boolean> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ConfigStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ConfigStatement} or {@code null} if not present}
         */
        default @Nullable ConfigStatement configStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ConfigStatement config) {
                    return config;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ConfigStatement}}
         */
        default @NonNull Optional<ConfigStatement> findConfigStatement() {
            return Optional.ofNullable(configStatement());
        }

        /**
         * {@return the {@code ConfigStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ConfigStatement getConfigStatement() {
            final var config = configStatement();
            if (config == null) {
                throw new NoSuchElementException("No config statement present in " + this);
            }
            return config;
        }
    }

    /**
     * The definition of {@code config} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Boolean, @NonNull ConfigStatement, @NonNull ConfigEffectiveStatement> DEF =
        StatementDefinition.of(ConfigStatement.class, ConfigEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "config", "value");

    @Override
    default StatementDefinition<Boolean, ?, ?> statementDefinition() {
        return DEF;
    }
}
