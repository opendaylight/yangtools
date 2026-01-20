/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class MaxAccessStatementSupport
        extends AbstractStatementSupport<MaxAccess, MaxAccessStatement, MaxAccessEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(MaxAccessStatement.DEFINITION).build();

    public MaxAccessStatementSupport(final YangParserConfiguration config) {
        super(MaxAccessStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    public MaxAccess parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final MaxAccess val = MaxAccess.forStringLiteral(value);
        if (val == null) {
            throw new SourceException(ctx, "Invalid max-access value '%s'", value);
        }
        return val;
    }

    @Override
    public String internArgument(final String rawArgument) {
        final MaxAccess val = MaxAccess.forStringLiteral(rawArgument);
        return val == null ? rawArgument : val.stringLiteral();
    }

    @Override
    protected MaxAccessStatement createDeclared(final BoundStmtCtx<MaxAccess> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new MaxAccessStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected MaxAccessStatement attachDeclarationReference(final MaxAccessStatement stmt,
            final DeclarationReference reference) {
        return new RefMaxAccessStatement(stmt, reference);
    }

    @Override
    protected MaxAccessEffectiveStatement createEffective(final Current<MaxAccess, MaxAccessStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new MaxAccessEffectiveStatementImpl(stmt, substatements);
    }
}