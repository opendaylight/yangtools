/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument.WithSubstatements;

public final class UnrecognizedStatementImpl extends WithSubstatements implements UnrecognizedStatement {
    private final @NonNull StatementDefinition definition;

    public UnrecognizedStatementImpl(final String rawArgument, final @NonNull StatementDefinition statementDefinition,
            final ImmutableList<? extends DeclaredStatement> substatements) {
        super(rawArgument, substatements);
        definition = requireNonNull(statementDefinition);
    }

    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }
}
