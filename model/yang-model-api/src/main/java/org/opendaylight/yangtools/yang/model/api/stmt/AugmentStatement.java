/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code augment} statement.
 */
public interface AugmentStatement extends DocumentedDeclaredStatement<SchemaNodeIdentifier>,
        DataDefinitionStatement.MultipleIn<SchemaNodeIdentifier>, ActionStatement.MultipleIn<SchemaNodeIdentifier>,
        CaseStatement.MultipleIn<SchemaNodeIdentifier>, IfFeatureStatement.MultipleIn<SchemaNodeIdentifier>,
        NotificationStatement.MultipleIn<SchemaNodeIdentifier>, StatusStatement.OptionalIn<SchemaNodeIdentifier>,
        WhenStatement.OptionalIn<SchemaNodeIdentifier> {
    /**
     * The definition of {@code augment} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<SchemaNodeIdentifier, @NonNull AugmentStatement, @NonNull AugmentEffectiveStatement>
        DEF = StatementDefinition.of(AugmentStatement.class, AugmentEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "augment", "target-node");

    @Override
    default StatementDefinition<SchemaNodeIdentifier, ?, ?> statementDefinition() {
        return DEF;
    }
}
