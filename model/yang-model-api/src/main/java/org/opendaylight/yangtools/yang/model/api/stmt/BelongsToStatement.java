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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code belongs-to} statement.
 */
public interface BelongsToStatement extends PrefixStatement.OptionalIn<Unqualified> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link BelongsToStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code BelongsToStatement} or {@code null} if not present}
         */
        default @Nullable BelongsToStatement belongsToStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof BelongsToStatement belongsTo) {
                    return belongsTo;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code BelongsToStatement}}
         */
        default @NonNull Optional<BelongsToStatement> findBelongsToStatement() {
            return Optional.ofNullable(belongsToStatement());
        }

        /**
         * {@return the {@code BelongsToStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull BelongsToStatement getBelongsToStatement() {
            final var belongsTo = belongsToStatement();
            if (belongsTo == null) {
                throw new NoSuchElementException("No belongs-to statement present in " + this);
            }
            return belongsTo;
        }
    }

    /**
     * The definition of {@code belongs-to} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Unqualified, @NonNull BelongsToStatement, @NonNull BelongsToEffectiveStatement> DEF =
        StatementDefinition.of(BelongsToStatement.class, BelongsToEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "belongs-to", YangArgumentDefinitions.MODULE_AS_UNQUALIFIED);

    @Override
    default StatementDefinition<Unqualified, ?, ?> statementDefinition() {
        return DEF;
    }
}
