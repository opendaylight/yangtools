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
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class AugmentIdentifierStatementSupport
        extends AbstractUnqualifiedStatementSupport<AugmentIdentifierStatement, AugmentIdentifierEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(AugmentIdentifierStatement.DEFINITION).build();

    public AugmentIdentifierStatementSupport(final YangParserConfiguration config) {
        super(AugmentIdentifierStatement.DEFINITION, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    public void onStatementAdded(
            final Mutable<Unqualified, AugmentIdentifierStatement, AugmentIdentifierEffectiveStatement> ctx) {
        final var parentDef = ctx.coerceParentContext().publicDefinition();
        final var parentRepr = parentDef.getDeclaredRepresentationClass();
        if (!AugmentStatement.class.isAssignableFrom(parentRepr)
            && !AugmentStructureStatement.class.isAssignableFrom(parentRepr)) {
            throw new SourceException(ctx, "augment-identifier cannot be defined in " + parentDef.statementName());
        }
    }

    @Override
    protected AugmentIdentifierStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new AugmentIdentifierStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected AugmentIdentifierStatement attachDeclarationReference(final AugmentIdentifierStatement stmt,
            final DeclarationReference reference) {
        return new RefAugmentIdentifierStatement(stmt, reference);
    }

    @Override
    protected AugmentIdentifierEffectiveStatement createEffective(
            final Current<Unqualified, AugmentIdentifierStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AugmentIdentifierEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
