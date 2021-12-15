/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class RpcStatementSupport extends AbstractSchemaTreeStatementSupport<RpcStatement, RpcEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.RPC)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.INPUT)
            .addOptional(YangStmtMapping.OUTPUT)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .build();

    public RpcStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.RPC, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, RpcStatement, RpcEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
            appendImplicitSubstatement(stmt, YangStmtMapping.INPUT.getStatementName());
        }
        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
            appendImplicitSubstatement(stmt, YangStmtMapping.OUTPUT.getStatementName());
        }
    }

    @Override
    protected RpcStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createRpc(ctx.getArgument(), substatements);
    }

    @Override
    protected RpcStatement attachDeclarationReference(final RpcStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateRpc(stmt, reference);
    }

    @Override
    protected RpcEffectiveStatement createEffective(final Current<QName, RpcStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        checkState(!substatements.isEmpty(), "Missing implicit input/output statements at %s", stmt.sourceReference());

        try {
            return EffectiveStatements.createRpc(stmt.declared(), substatements, stmt.getArgument(),
                computeFlags(substatements));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }

    private static void appendImplicitSubstatement(final Mutable<QName, RpcStatement, RpcEffectiveStatement> stmt,
            final QName substatementName) {
        stmt.addEffectiveSubstatement(
            verifyNotNull(stmt.getFromNamespace(StatementSupportNamespace.class, substatementName)), null);
    }
}
