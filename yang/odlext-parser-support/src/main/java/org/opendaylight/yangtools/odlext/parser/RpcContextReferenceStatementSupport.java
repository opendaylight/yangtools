/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.odlext.model.api.RpcContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.RpcContextReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class RpcContextReferenceStatementSupport
        extends AbstractStringStatementSupport<RpcContextReferenceStatement, RpcContextReferenceEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.RPC_CONTEXT_REFERENCE).build();

    public RpcContextReferenceStatementSupport(final YangParserConfiguration config) {
        super(OpenDaylightExtensionsStatements.RPC_CONTEXT_REFERENCE, StatementPolicy.contextIndependent(), config);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected RpcContextReferenceStatement createDeclared(
            final StmtContext<String, RpcContextReferenceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RpcContextReferenceStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected RpcContextReferenceEffectiveStatement createEffective(
            final Current<String, RpcContextReferenceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RpcContextReferenceEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
