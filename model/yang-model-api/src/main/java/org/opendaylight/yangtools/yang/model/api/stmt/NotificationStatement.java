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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code notification} statement.
 */
public interface NotificationStatement extends DataDefinitionStatement.MultipleIn<QName>,
        DescriptionStatement.OptionalIn<QName>, GroupingStatementMultipleIn<QName>,
        IfFeatureStatement.MultipleIn<QName>, MustStatement.MultipleIn<QName>, ReferenceStatement.OptionalIn<QName>,
        StatusStatement.OptionalIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link IfFeatureStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code NotificationStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull NotificationStatement> notificationStatements() {
            return declaredSubstatements(NotificationStatement.class);
        }
    }

    /**
     * The definition of {@code notification} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull NotificationStatement, @NonNull NotificationEffectiveStatement> DEF =
        StatementDefinition.of(NotificationStatement.class, NotificationEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "notification", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
