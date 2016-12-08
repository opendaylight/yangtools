/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective.ModifierEffectiveStatementImpl;

public class ModifierStatementImpl extends AbstractDeclaredStatement<ModifierKind> implements ModifierStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            YangStmtMapping.MODIFIER).build();

    protected ModifierStatementImpl(final StmtContext<ModifierKind, ModifierStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<ModifierKind, ModifierStatement, EffectiveStatement<ModifierKind, ModifierStatement>> {

        public Definition() {
            super(YangStmtMapping.MODIFIER);
        }

        @Override
        public ModifierKind parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            final ModifierKind argument = ModifierKind.fromKeyword(value);
            SourceException.throwIfNull(argument, ctx.getStatementSourceReference(),
                    "'%s' is not valid argument of modifier statement", value);
            return argument;
        }

        @Override
        public ModifierStatement createDeclared(final StmtContext<ModifierKind, ModifierStatement, ?> ctx) {
            return new ModifierStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<ModifierKind, ModifierStatement> createEffective(
                final StmtContext<ModifierKind, ModifierStatement, EffectiveStatement<ModifierKind, ModifierStatement>> ctx) {
            return new ModifierEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<ModifierKind, ModifierStatement, EffectiveStatement<ModifierKind, ModifierStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public ModifierKind getValue() {
        return argument();
    }
}
