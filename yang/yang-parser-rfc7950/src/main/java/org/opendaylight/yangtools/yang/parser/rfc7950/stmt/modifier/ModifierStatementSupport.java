/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.modifier;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ModifierStatementSupport
        extends BaseStatementSupport<ModifierKind, ModifierStatement, ModifierEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MODIFIER).build();
    private static final ModifierStatementSupport INSTANCE = new ModifierStatementSupport();

    private ModifierStatementSupport() {
        super(YangStmtMapping.MODIFIER);
    }

    public static ModifierStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public ModifierKind parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SourceException.unwrap(ModifierKind.parse(value), ctx.getStatementSourceReference(),
            "'%s' is not valid argument of modifier statement", value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public String internArgument(final String rawArgument) {
        return "invert-match".equals(rawArgument) ? "invert-match" : rawArgument;
    }

    @Override
    protected ModifierStatement createDeclared(final StmtContext<ModifierKind, ModifierStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularModifierStatement(ctx, substatements);
    }

    @Override
    protected ModifierStatement createEmptyDeclared(final StmtContext<ModifierKind, ModifierStatement, ?> ctx) {
        return new EmptyModifierStatement(ctx);
    }

    @Override
    protected ModifierEffectiveStatement createEffective(
            final StmtContext<ModifierKind, ModifierStatement, ModifierEffectiveStatement> ctx,
            final ModifierStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularModifierEffectiveStatement(declared, substatements);
    }

    @Override
    protected ModifierEffectiveStatement createEmptyEffective(
            final StmtContext<ModifierKind, ModifierStatement, ModifierEffectiveStatement> ctx,
            final ModifierStatement declared) {
        return new EmptyModifierEffectiveStatement(declared);
    }
}
