/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective representation of a {@code key} statement.
 */
public interface KeyEffectiveStatement extends EffectiveStatement<KeyArgument, @NonNull KeyStatement> {
    /**
     * An {@link EffectiveStatement} that is a parent of a single {@link KeyEffectiveStatement}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     * @since 15.0.1
     */
    @Beta
    interface OptionalIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * {@return the {@code KeyEffectiveStatement} or {@code null} if not present}
         */
        default @Nullable KeyEffectiveStatement keyStatement() {
            for (var stmt : effectiveSubstatements()) {
                if (stmt instanceof KeyEffectiveStatement key) {
                    return key;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code KeyEffectiveStatement}}
         */
        default @NonNull Optional<KeyEffectiveStatement> findKeyStatement() {
            return Optional.ofNullable(keyStatement());
        }

        /**
         * {@return the {@code KeyEffectiveStatement}}
         *
         * @throws NoSuchElementException if not present
         */
        default @NonNull KeyEffectiveStatement getKeyStatement() {
            final var key = keyStatement();
            if (key == null) {
                throw new NoSuchElementException("No key statement present in " + this);
            }
            return key;
        }
    }

    @Override
    default StatementDefinition<KeyArgument, @NonNull KeyStatement, ?> statementDefinition() {
        return KeyStatement.DEF;
    }
}
