/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code bit} statement.
 */
public interface BitStatement extends DocumentedDeclaredStatement<String>, IfFeatureStatement.MultipleIn<String>,
        StatusStatement.OptionalIn<String> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link BitStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code BitStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull BitStatement> bitStatements() {
            return declaredSubstatements(BitStatement.class);
        }
    }

    /**
     * The definition of {@code bit} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull BitStatement, @NonNull BitEffectiveStatement> DEF =
        StatementDefinition.of(BitStatement.class, BitEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "bit", "name");

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }

    default @Nullable PositionStatement getPosition() {
        final var opt = findFirstDeclaredSubstatement(PositionStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
