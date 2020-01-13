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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

abstract class AbstractContainerStatementSupport
        extends BaseQNameStatementSupport<ContainerStatement, ContainerEffectiveStatement> {
    AbstractContainerStatementSupport() {
        super(YangStmtMapping.CONTAINER);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, ContainerStatement, ContainerEffectiveStatement> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    public final ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement,?> ctx) {
        return new ContainerStatementImpl(ctx);
    }

    @Override
    protected final ContainerEffectiveStatement createEffective(
            final StmtContext<QName, ContainerStatement, ContainerEffectiveStatement> ctx,
            final ContainerStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {

        final StatementSourceReference ref = ctx.getStatementSourceReference();
        final SchemaPath path = ctx.getSchemaPath().get();
        final ContainerSchemaNode original = (ContainerSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective)
                .orElse(null);
        final int flags = new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration())
                .setPresence(findFirstStatement(substatements, PresenceEffectiveStatement.class) != null)
                .toFlags();
        return new ContainerEffectiveStatementImpl(declared, path, flags, ref, substatements, original);
    }

    @Override
    protected final ContainerEffectiveStatement createEmptyEffective(
            final StmtContext<QName, ContainerStatement, ContainerEffectiveStatement> ctx,
            final ContainerStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
