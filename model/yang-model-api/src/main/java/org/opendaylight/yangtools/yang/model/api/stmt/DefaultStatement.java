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
 * Declared representation of a {@code default} statement.
 */
public interface DefaultStatement extends DeclaredStatement<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link DefaultStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code DefaultStatement} or {@code null} if not present}
         */
        default @Nullable DefaultStatement defaultStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof DefaultStatement dflt) {
                    return dflt;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code DefaultStatement}}
         */
        default @NonNull Optional<DefaultStatement> findDefaultStatement() {
            return Optional.ofNullable(defaultStatement());
        }

        /**
         * {@return the {@code DefaultStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull DefaultStatement getDefaultStatement() {
            final var length = defaultStatement();
            if (length == null) {
                throw new NoSuchElementException("No default statement present in " + this);
            }
            return length;
        }
    }

    /**
     * The definition of {@code default} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull DefaultStatement, @NonNull DefaultEffectiveStatement> DEF =
        StatementDefinition.of(DefaultStatement.class, DefaultEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "default", YangArgumentDefinitions.VALUE_AS_STRING);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
