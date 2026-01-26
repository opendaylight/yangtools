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
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code key} statement.
 */
public interface KeyStatement extends DeclaredStatement<KeyArgument> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link KeyStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code KeyStatement} or {@code null} if not present}
         */
        default @Nullable KeyStatement keyStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof KeyStatement key) {
                    return key;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code KeyStatement}}
         */
        default @NonNull Optional<KeyStatement> findKeyStatement() {
            return Optional.ofNullable(keyStatement());
        }

        /**
         * {@return the {@code KeyStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull KeyStatement getKeyStatement() {
            final var key = keyStatement();
            if (key == null) {
                throw new NoSuchElementException("No key statement present in " + this);
            }
            return key;
        }
    }

    /**
     * The definition of {@code key} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<KeyArgument, @NonNull KeyStatement, @NonNull KeyEffectiveStatement> DEF =
        StatementDefinition.of(KeyStatement.class, KeyEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "key",
            ArgumentDefinition.of(KeyArgument.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<KeyArgument, ?, ?> statementDefinition() {
        return DEF;
    }
}
