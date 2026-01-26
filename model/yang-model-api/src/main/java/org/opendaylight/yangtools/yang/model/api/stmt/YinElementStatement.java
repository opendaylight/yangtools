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
 * Declared representation of a {@code yin-element} statement.
 */
public interface YinElementStatement extends DeclaredStatement<Boolean> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link YinElementStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code YinElementStatement} or {@code null} if not present}
         */
        default @Nullable YinElementStatement yinElementStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof YinElementStatement yinElement) {
                    return yinElement;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code YinElementStatement}}
         */
        default @NonNull Optional<YinElementStatement> findYinElementStatement() {
            return Optional.ofNullable(yinElementStatement());
        }

        /**
         * {@return the {@code YinElementStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull YinElementStatement getYinElementStatement() {
            final var yinElement = yinElementStatement();
            if (yinElement == null) {
                throw new NoSuchElementException("No yin-element statement present in " + this);
            }
            return yinElement;
        }
    }

    /**
     * The definition of {@code yin-element} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Boolean, @NonNull YinElementStatement, @NonNull YinElementEffectiveStatement> DEF =
        StatementDefinition.of(YinElementStatement.class, YinElementEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "yin-element", YangArgumentDefinitions.VALUE_AS_BOOLEAN);

    @Override
    default StatementDefinition<Boolean, ?, ?> statementDefinition() {
        return DEF;
    }
}
