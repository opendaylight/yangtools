/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

@NonNullByDefault
final class OpenConfigHashedValueEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<Empty, OpenConfigHashedValueStatement>
        implements OpenConfigHashedValueEffectiveStatement {
    private final StatementDefinition definition;

    OpenConfigHashedValueEffectiveStatementImpl(final Current<Empty, OpenConfigHashedValueStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
        definition = stmt.publicDefinition();
    }

    @Override
    public QName getQName() {
        return definition.statementName();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }

    @Override
    public OpenConfigHashedValueEffectiveStatement asEffectiveStatement() {
        return this;
    }
}