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
import org.opendaylight.yangtools.rfc6643.model.api.DefValEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DefValStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DefValStatementSupport
        extends AbstractStringStatementSupport<DefValStatement, DefValEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
            SubstatementValidator.builder(DefValStatement.DEFINITION).build();

    public DefValStatementSupport(final YangParserConfiguration config) {
        super(DefValStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected DefValStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new DefValStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DefValStatement attachDeclarationReference(final DefValStatement stmt,
            final DeclarationReference reference) {
        return new RefDefValStatement(stmt, reference);
    }

    @Override
    protected DefValEffectiveStatement createEffective(final Current<String, DefValStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DefValEffectiveStatementImpl(stmt, substatements);
    }
}