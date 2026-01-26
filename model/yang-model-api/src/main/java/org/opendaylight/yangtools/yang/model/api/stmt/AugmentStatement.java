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
 * Declared representation of a {@code augment} statement.
 */
public interface AugmentStatement extends DeclaredStatement<SchemaNodeIdentifier>,
        DataDefinitionStatement.MultipleIn<SchemaNodeIdentifier>, ActionStatement.MultipleIn<SchemaNodeIdentifier>,
        CaseStatement.MultipleIn<SchemaNodeIdentifier>, DescriptionStatement.OptionalIn<SchemaNodeIdentifier>,
        IfFeatureStatement.MultipleIn<SchemaNodeIdentifier>, NotificationStatement.MultipleIn<SchemaNodeIdentifier>,
        ReferenceStatement.OptionalIn<SchemaNodeIdentifier>, StatusStatement.OptionalIn<SchemaNodeIdentifier>,
        WhenStatement.OptionalIn<SchemaNodeIdentifier> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link AugmentStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code AugmentStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull AugmentStatement> augmentStatements() {
            return declaredSubstatements(AugmentStatement.class);
        }
    }

    /**
     * The definition of {@code augment} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<SchemaNodeIdentifier, @NonNull AugmentStatement, @NonNull AugmentEffectiveStatement>
        DEF = StatementDefinition.of(AugmentStatement.class, AugmentEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "augment",
            ArgumentDefinition.of(SchemaNodeIdentifier.class, YangConstants.RFC6020_YIN_MODULE, "target-node"));

    @Override
    default StatementDefinition<SchemaNodeIdentifier, ?, ?> statementDefinition() {
        return DEF;
    }
}
