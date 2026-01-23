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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Declared representation of a {@code unique} statement.
 */
// FIXME: UniqueAargument instead of Set<SchemaNodeIdentifier.Descendant>
public interface UniqueStatement extends DeclaredStatement<Set<SchemaNodeIdentifier.Descendant>> {
    /**
     * The definition of {@code unique} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Set<Descendant>, UniqueStatement, UniqueEffectiveStatement> DEF =
        StatementDefinition.of(UniqueStatement.class, UniqueEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "unique", "tag");

    @Override
    default StatementDefinition<Set<Descendant>, UniqueStatement, UniqueEffectiveStatement> statementDefinition() {
        return DEF;
    }
}
