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
import org.opendaylight.yangtools.odlext.model.api.LegacyAugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.LegacyAugmentIdentifierStatement;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class LegacyAugmentIdentifierStatementSupport
        extends AbstractUnqualifiedStatementSupport<LegacyAugmentIdentifierStatement,
            LegacyAugmentIdentifierEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(LegacyAugmentIdentifierStatement.DEFINITION).build();

    public LegacyAugmentIdentifierStatementSupport(final YangParserConfiguration config) {
        super(LegacyAugmentIdentifierStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected LegacyAugmentIdentifierStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new LegacyAugmentIdentifierStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected LegacyAugmentIdentifierStatement attachDeclarationReference(final LegacyAugmentIdentifierStatement stmt,
            final DeclarationReference reference) {
        return new RefLegacyAugmentIdentifierStatement(stmt, reference);
    }

    @Override
    protected LegacyAugmentIdentifierEffectiveStatement createEffective(
            final Current<Unqualified, LegacyAugmentIdentifierStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new LegacyAugmentIdentifierEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
