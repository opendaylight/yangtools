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
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.AliasStatement;
import org.opendaylight.yangtools.rfc6643.model.api.OidStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class AliasStatementSupport
        extends AbstractStringStatementSupport<AliasStatement, AliasEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(AliasStatement.DEFINITION)
        .addOptional(DescriptionStatement.DEFINITION)
        .addOptional(ReferenceStatement.DEFINITION)
        .addOptional(StatusStatement.DEFINITION)
        .addOptional(OidStatement.DEFINITION)
        .build();

    public AliasStatementSupport(final YangParserConfiguration config) {
        super(AliasStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected AliasStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new AliasStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected AliasStatement attachDeclarationReference(final AliasStatement stmt,
            final DeclarationReference reference) {
        return new RefAliasStatement(stmt, reference);
    }

    @Override
    protected AliasEffectiveStatement createEffective(final Current<String, AliasStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AliasEffectiveStatementImpl(stmt, substatements);
    }
}
