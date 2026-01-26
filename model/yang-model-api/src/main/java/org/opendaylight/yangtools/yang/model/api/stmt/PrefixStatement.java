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
 * Declared representation of a {@code prefix} statement.
 */
public interface PrefixStatement extends DeclaredStatement<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link PrefixStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code PrefixStatement} or {@code null} if not present}
         */
        default @Nullable PrefixStatement prefixStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof PrefixStatement prefix) {
                    return prefix;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code PrefixStatement}}
         */
        default @NonNull Optional<PrefixStatement> findPrefixStatement() {
            return Optional.ofNullable(prefixStatement());
        }

        /**
         * {@return the {@code PrefixStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull PrefixStatement getPrefixStatement() {
            final var prefix = prefixStatement();
            if (prefix == null) {
                throw new NoSuchElementException("No prefix statement present in " + this);
            }
            return prefix;
        }
    }

    /**
     * The definition of {@code prefix} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull PrefixStatement, @NonNull PrefixEffectiveStatement> DEF =
        StatementDefinition.of(PrefixStatement.class, PrefixEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "prefix", "value");

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
