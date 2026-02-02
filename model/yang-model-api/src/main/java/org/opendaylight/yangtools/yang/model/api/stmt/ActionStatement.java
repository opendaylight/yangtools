/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Represents YANG {@code action} statement.
 *
 * <p>The "action" statement is used to define an operation connected to a specific container or list data node. It
 * takes one argument, which is an identifier, followed by a block of substatements that holds detailed action
 * information. The argument is the name of the action.
 */
public interface ActionStatement extends DescriptionStatement.OptionalIn<QName>, IfFeatureStatement.MultipleIn<QName>,
        GroupingStatementMultipleIn<QName>, InputStatement.OptionalIn<QName>, OutputStatement.OptionalIn<QName>,
        ReferenceStatement.OptionalIn<QName>, StatusStatement.OptionalIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link ActionStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code ActionStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull ActionStatement> actionStatements() {
            return declaredSubstatements(ActionStatement.class);
        }
    }

    /**
     * The definition of {@code action} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ActionStatement, @NonNull ActionEffectiveStatement> DEF =
        StatementDefinition.of(ActionStatement.class, ActionEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "action", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
