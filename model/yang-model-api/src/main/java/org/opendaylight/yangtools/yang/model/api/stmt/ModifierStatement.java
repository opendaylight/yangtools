/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;

/**
 * Represents YANG modifier statement.
 *
 * <p>The "modifier" statement, which is an optional substatement to the "pattern" statement, takes as an argument
 * the string "invert-match". If a pattern has the "invert-match" modifier present, the type is restricted to values
 * that do not match the pattern.
 */
public interface ModifierStatement extends DeclaredStatement<ModifierKind> {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link ModifierStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code ModifierStatement} or {@code null} if not present}
         */
        default @Nullable ModifierStatement modifierStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof ModifierStatement modifier) {
                    return modifier;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code ModifierStatement}}
         */
        default @NonNull Optional<ModifierStatement> findModifierStatement() {
            return Optional.ofNullable(modifierStatement());
        }

        /**
         * {@return the {@code ModifierStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull ModifierStatement getModifierStatement() {
            final var modifier = modifierStatement();
            if (modifier == null) {
                throw new NoSuchElementException("No modifier statement present in " + this);
            }
            return modifier;
        }
    }

    /**
     * The definition of {@code modifier} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<ModifierKind, @NonNull ModifierStatement, @NonNull ModifierEffectiveStatement> DEF =
        StatementDefinition.of(ModifierStatement.class, ModifierEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "modifier",
            ArgumentDefinition.of(ModifierKind.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<ModifierKind, ?, ?> statementDefinition() {
        return DEF;
    }
}
