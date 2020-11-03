/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class UnrecognizedStatementImpl extends WithSubstatements implements UnrecognizedStatement {
    private final @NonNull StatementDefinition definition;

    /**
     * Deprecated.
     *
     * @deprecated Use {@link UnrecognizedStatementImpl#UnrecognizedStatementImpl(String, ImmutableList,
     * StatementDefinition)} instead
     */
    @Deprecated(forRemoval = true)
    UnrecognizedStatementImpl(final StmtContext<String, ?, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context.rawStatementArgument(), substatements);
        this.definition = context.getPublicDefinition();
    }

    UnrecognizedStatementImpl(final @NonNull String rawArgument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements,
            final @NonNull StatementDefinition statementDefinition) {
        super(rawArgument, substatements);
        this.definition = requireNonNull(statementDefinition);
    }

    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }
}
