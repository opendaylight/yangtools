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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredHumanTextStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code description} statement.
 */
public interface DescriptionStatement extends DeclaredHumanTextStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of a single {@link DescriptionStatement}.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface OptionalIn<A> extends DeclaredStatement<A> {
        /**
         * {@return the {@code DescriptionStatement} or {@code null} if not present}
         */
        default @Nullable DescriptionStatement descriptionStatement() {
            for (var stmt : declaredSubstatements()) {
                if (stmt instanceof DescriptionStatement description) {
                    return description;
                }
            }
            return null;
        }

        /**
         * {@return an optional {@code DescriptionStatement}}
         */
        default @NonNull Optional<DescriptionStatement> findDescriptionStatement() {
            return Optional.ofNullable(descriptionStatement());
        }

        /**
         * {@return the {@code DescriptionStatement}}
         * @throws NoSuchElementException if not present
         */
        default @NonNull DescriptionStatement getDescriptionStatement() {
            final var description = descriptionStatement();
            if (description == null) {
                throw new NoSuchElementException("No status statement present in " + this);
            }
            return description;
        }
    }

    /**
     * The definition of {@code description} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull DescriptionStatement, @NonNull DescriptionEffectiveStatement> DEF =
        StatementDefinition.of(DescriptionStatement.class, DescriptionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "description", "text", true);

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
