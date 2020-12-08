/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

@Beta
public final class RpcStatementSupport extends BaseSchemaTreeStatementSupport<RpcStatement, RpcEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.RPC)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.INPUT)
        .addOptional(YangStmtMapping.OUTPUT)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .build();
    private static final @NonNull RpcStatementSupport RFC6020_INSTANCE = new RpcStatementSupport(
        InputStatementSupport.rfc6020Instance(), OutputStatementSupport.rfc6020Instance());
    private static final @NonNull RpcStatementSupport RFC7950_INSTANCE = new RpcStatementSupport(
        InputStatementSupport.rfc7950Instance(), OutputStatementSupport.rfc7950Instance());

    private final InputStatementSupport implicitInput;
    private final OutputStatementSupport implicitOutput;

    private RpcStatementSupport(final InputStatementSupport implicitInput,
            final OutputStatementSupport implicitOutput) {
        super(YangStmtMapping.RPC, CopyPolicy.DECLARED_COPY);
        this.implicitInput = requireNonNull(implicitInput);
        this.implicitOutput = requireNonNull(implicitOutput);
    }

    public static @NonNull RpcStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull RpcStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, RpcStatement, RpcEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        verify(stmt instanceof StatementContextBase);
        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitSubstatement(implicitInput, null);
        }
        if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
            ((StatementContextBase<?, ?, ?>) stmt).appendImplicitSubstatement(implicitOutput, null);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected RpcStatement createDeclared(final StmtContext<QName, RpcStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularRpcStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected RpcStatement createEmptyDeclared(final StmtContext<QName, RpcStatement, ?> ctx) {
        return new EmptyRpcStatement(ctx.getArgument());
    }

    @Override
    protected RpcEffectiveStatement createEffective(final Current<QName, RpcStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        checkState(!substatements.isEmpty(), "Missing implicit input/output statements at %s", stmt.sourceReference());

        try {
            return new RpcEffectiveStatementImpl(stmt.declared(), substatements, computeFlags(substatements),
                stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
