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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Declared representation of a {@code must} statement.
 */
public interface MustStatement extends DocumentedDeclaredStatement<QualifiedBound>,
        ErrorAppTagStatement.OptionalIn<QualifiedBound>, ErrorMessageStatement.OptionalIn<QualifiedBound> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link MustStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code MustStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull MustStatement> mustStatements() {
            return declaredSubstatements(MustStatement.class);
        }
    }

    /**
     * The definition of {@code must} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QualifiedBound, @NonNull MustStatement, @NonNull MustEffectiveStatement> DEF =
        StatementDefinition.of(MustStatement.class, MustEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "must", "condition");

    @Override
    default StatementDefinition<QualifiedBound, ?, ?> statementDefinition() {
        return DEF;
    }
}
