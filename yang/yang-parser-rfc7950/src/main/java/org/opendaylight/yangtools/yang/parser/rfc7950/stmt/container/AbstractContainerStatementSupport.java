/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractContainerStatementSupport
        extends BaseSchemaTreeStatementSupport<ContainerStatement, ContainerEffectiveStatement> {
    AbstractContainerStatementSupport() {
        super(YangStmtMapping.CONTAINER);
    }

    @Override
    protected final ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularContainerStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final ContainerStatement createEmptyDeclared(final StmtContext<QName, ContainerStatement, ?> ctx) {
        return new EmptyContainerStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected ContainerEffectiveStatement createEffective(final Current<QName, ContainerStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final SchemaPath path = stmt.getSchemaPath();
        final ContainerSchemaNode original = (ContainerSchemaNode) stmt.original();
        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig())
                .setPresence(findFirstStatement(substatements, PresenceEffectiveStatement.class) != null)
                .toFlags();

        final var ref = stmt.sourceReference();
        final var rabbit = stmt.caerbannog();
        EffectiveStmtUtils.checkUniqueGroupings(rabbit, substatements, ref);
        EffectiveStmtUtils.checkUniqueTypedefs(rabbit, substatements, ref);
        EffectiveStmtUtils.checkUniqueUses(rabbit, substatements, ref);

        return new ContainerEffectiveStatementImpl(stmt.declared(), substatements, stmt.sourceReference(), flags, path,
            original);
    }
}
