/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultStatementSupport
        extends AbstractStringStatementSupport<DefaultStatement, DefaultEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(DefaultStatement.DEFINITION).build();

    public DefaultStatementSupport(final YangParserConfiguration config) {
        // Note: if we start interpreting the string we'll need to use StatementPolicy.declaredCopy()
        super(DefaultStatement.DEFINITION, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected DefaultStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createDefault(ctx.getRawArgument(), substatements);
    }

    @Override
    protected DefaultStatement attachDeclarationReference(final DefaultStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateDefault(stmt, reference);
    }

    @Override
    protected DefaultEffectiveStatement createEffective(final Current<String, DefaultStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createDefault(stmt.declared(), substatements);
    }
}
