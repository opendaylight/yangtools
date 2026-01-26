/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Declared representation of a {@code unique} statement.
 */
// FIXME: UniqueArgument instead of Set<SchemaNodeIdentifier.Descendant>
public interface UniqueStatement extends DeclaredStatement<Set<SchemaNodeIdentifier.Descendant>> {
    /**
     * The definition of {@code unique} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Set<Descendant>, @NonNull UniqueStatement, @NonNull UniqueEffectiveStatement> DEF =
        StatementDefinition.of(UniqueStatement.class, UniqueEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "unique",
            ArgumentDefinition.of(Set.class, YangConstants.RFC6020_YIN_MODULE, "tag"));

    @Override
    default StatementDefinition<Set<Descendant>, ?, ?> statementDefinition() {
        return DEF;
    }
}
