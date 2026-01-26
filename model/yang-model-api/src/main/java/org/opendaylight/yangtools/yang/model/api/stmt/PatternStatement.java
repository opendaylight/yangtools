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
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code pattern} statement.
 */
public interface PatternStatement extends DeclaredStatement<PatternExpression>,
        DescriptionStatement.OptionalIn<PatternExpression>, ErrorAppTagStatement.OptionalIn<PatternExpression>,
        ErrorMessageStatement.OptionalIn<PatternExpression>, ModifierStatement.OptionalIn<PatternExpression>,
        ReferenceStatement.OptionalIn<PatternExpression> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link PatternStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code PatternStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull PatternStatement> patternStatements() {
            return declaredSubstatements(PatternStatement.class);
        }
    }

    /**
     * The definition of {@code pattern} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<PatternExpression, @NonNull PatternStatement, @NonNull PatternEffectiveStatement> DEF =
        StatementDefinition.of(PatternStatement.class, PatternEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "pattern",
            ArgumentDefinition.of(PatternExpression.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<PatternExpression, ?, ?> statementDefinition() {
        return DEF;
    }
}
