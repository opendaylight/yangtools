/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ImpliedStatementSupport
        extends AbstractStringStatementSupport<ImpliedStatement, ImpliedEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(ImpliedStatement.DEFINITION).build();

    public ImpliedStatementSupport(final YangParserConfiguration config) {
        super(ImpliedStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected ImpliedStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new ImpliedStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected ImpliedStatement attachDeclarationReference(final ImpliedStatement stmt,
            final DeclarationReference reference) {
        return new RefImpliedStatement(stmt, reference);
    }

    @Override
    protected ImpliedEffectiveStatement createEffective(final Current<String, ImpliedStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ImpliedEffectiveStatementImpl(stmt, substatements);
    }
}