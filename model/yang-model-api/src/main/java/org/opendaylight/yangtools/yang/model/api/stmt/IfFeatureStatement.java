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
 * Represents YANG if-feature statement. The "if-feature" statement makes its parent statement conditional.
 */
public interface IfFeatureStatement extends DeclaredStatement<IfFeatureExpr> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link IfFeatureStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code IfFeatureStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull IfFeatureStatement> ifFeatureStatements() {
            return declaredSubstatements(IfFeatureStatement.class);
        }
    }

    /**
     * The definition of {@code if-feature} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<IfFeatureExpr, @NonNull IfFeatureStatement, @NonNull IfFeatureEffectiveStatement> DEF =
        StatementDefinition.of(IfFeatureStatement.class, IfFeatureEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "if-feature",
            ArgumentDefinition.of(IfFeatureExpr.class, YangConstants.RFC6020_YIN_MODULE, "name"));

    @Override
    default StatementDefinition<IfFeatureExpr, ?, ?> statementDefinition() {
        return DEF;
    }
}
