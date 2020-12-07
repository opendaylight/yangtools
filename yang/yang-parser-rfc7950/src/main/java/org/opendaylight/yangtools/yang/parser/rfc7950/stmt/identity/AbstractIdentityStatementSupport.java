/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractIdentityStatementSupport
        extends BaseQNameStatementSupport<IdentityStatement, IdentityEffectiveStatement> {

    AbstractIdentityStatementSupport() {
        super(YangStmtMapping.IDENTITY, CopyPolicy.DECLARED_COPY);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final void onStatementDefinitionDeclared(
            final Mutable<QName, IdentityStatement, IdentityEffectiveStatement> stmt) {
        final QName qname = stmt.getArgument();
        final StmtContext<?, ?, ?> prev = stmt.getFromNamespace(IdentityNamespace.class, qname);
        SourceException.throwIf(prev != null, stmt, "Duplicate identity definition %s", qname);
        stmt.addToNs(IdentityNamespace.class, qname, stmt);
    }

    @Override
    protected final IdentityStatement createDeclared(final StmtContext<QName, IdentityStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularIdentityStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected final IdentityStatement createEmptyDeclared(@NonNull final StmtContext<QName, IdentityStatement, ?> ctx) {
        return new EmptyIdentityStatement(ctx.getArgument());
    }

    @Override
    protected final IdentityEffectiveStatement createEffective(final Current<QName, IdentityStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            return new EmptyIdentityEffectiveStatement(stmt.declared(), stmt.wrapSchemaPath());
        }

        final List<IdentitySchemaNode> identities = new ArrayList<>();
        for (EffectiveStatement<?, ?> substatement : substatements) {
            if (substatement instanceof BaseEffectiveStatement) {
                final QName qname = ((BaseEffectiveStatement) substatement).argument();
                final IdentityEffectiveStatement identity =
                        verifyNotNull(stmt.getFromNamespace(IdentityNamespace.class, qname),
                            "Failed to find identity %s", qname)
                        .buildEffective();
                verify(identity instanceof IdentitySchemaNode, "%s is not a IdentitySchemaNode", identity);
                identities.add((IdentitySchemaNode) identity);
            }
        }

        return new RegularIdentityEffectiveStatement(stmt.declared(), stmt.wrapSchemaPath(), new FlagsBuilder()
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .toFlags(), substatements, ImmutableSet.copyOf(identities));
    }

    @Override
    public @NonNull boolean copyEffective(final IdentityEffectiveStatement original,
                                          final Current<QName, IdentityStatement> stmt) {
        return ((EmptyIdentityEffectiveStatement)original).getPath().equals(stmt.wrapSchemaPath());
    }
}
