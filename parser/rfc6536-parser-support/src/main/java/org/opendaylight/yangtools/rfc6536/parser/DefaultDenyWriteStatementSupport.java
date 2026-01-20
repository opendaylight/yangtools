/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyWriteStatementSupport
        extends AbstractEmptyStatementSupport<DefaultDenyWriteStatement, DefaultDenyWriteEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(DefaultDenyWriteStatement.DEFINITION).build();

    public DefaultDenyWriteStatementSupport(final YangParserConfiguration config) {
        super(DefaultDenyWriteStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected DefaultDenyWriteStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? DefaultDenyWriteStatementImpl.EMPTY
            : new DefaultDenyWriteStatementImpl(substatements);
    }

    @Override
    protected DefaultDenyWriteStatement attachDeclarationReference(final DefaultDenyWriteStatement stmt,
            final DeclarationReference reference) {
        return new RefDefaultDenyWriteStatement(stmt, reference);
    }

    @Override
    protected DefaultDenyWriteEffectiveStatement createEffective(final Current<Empty, DefaultDenyWriteStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DefaultDenyWriteEffectiveStatementImpl(stmt, substatements);
    }
}
