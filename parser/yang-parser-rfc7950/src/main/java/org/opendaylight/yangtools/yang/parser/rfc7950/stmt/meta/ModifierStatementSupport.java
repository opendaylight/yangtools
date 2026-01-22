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
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ModifierStatementSupport
        extends AbstractStatementSupport<ModifierKind, ModifierStatement, ModifierEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(ModifierStatement.DEFINITION).build();

    public ModifierStatementSupport(final YangParserConfiguration config) {
        super(ModifierStatement.DEFINITION, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public ModifierKind parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SourceException.unwrap(ModifierKind.parse(value), ctx,
            "'%s' is not valid argument of modifier statement", value);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return "invert-match".equals(rawArgument) ? "invert-match" : rawArgument;
    }

    @Override
    protected ModifierStatement createDeclared(final BoundStmtCtx<ModifierKind> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createModifier(ctx.getArgument(), substatements);
    }

    @Override
    protected ModifierStatement attachDeclarationReference(final ModifierStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateModifier(stmt, reference);
    }

    @Override
    protected ModifierEffectiveStatement createEffective(final Current<ModifierKind, ModifierStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createModifier(stmt.declared(), substatements);
    }
}
