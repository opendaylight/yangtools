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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Declared representation of a {@code refine} statement.
 */
public interface RefineStatement extends DeclaredStatement<Descendant>, ConfigStatement.OptionalIn<Descendant>,
        DefaultStatement.MultipleIn<Descendant>, DescriptionStatement.OptionalIn<Descendant>,
        IfFeatureStatement.MultipleIn<Descendant>, MandatoryStatement.OptionalIn<Descendant>,
        MaxElementsStatement.OptionalIn<Descendant>, MinElementsStatement.OptionalIn<Descendant>,
        MustStatement.MultipleIn<Descendant>, PresenceStatement.OptionalIn<Descendant>,
        ReferenceStatement.OptionalIn<Descendant> {
    /**
     * The definition of {@code refine} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Descendant, @NonNull RefineStatement, @NonNull RefineEffectiveStatement> DEF =
        StatementDefinition.of(RefineStatement.class, RefineEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "refine", "target-node");

    @Override
    default StatementDefinition<Descendant, ?, ?> statementDefinition() {
        return DEF;
    }
}
