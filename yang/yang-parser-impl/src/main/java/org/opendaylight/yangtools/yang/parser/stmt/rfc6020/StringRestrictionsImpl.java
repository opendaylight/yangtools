/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringRestrictionsEffectiveStatementImpl;

public class StringRestrictionsImpl extends AbstractDeclaredStatement<String> implements
        TypeStatement.StringRestrictions {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addOptional(YangStmtMapping.LENGTH)
            .addAny(YangStmtMapping.PATTERN)
            .build();

    protected StringRestrictionsImpl(final StmtContext<String, TypeStatement.StringRestrictions, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement.StringRestrictions, EffectiveStatement<String, TypeStatement.StringRestrictions>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement.StringRestrictions createDeclared(
                final StmtContext<String, TypeStatement.StringRestrictions, ?> ctx) {
            return new StringRestrictionsImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.StringRestrictions> createEffective(
                final StmtContext<String, TypeStatement.StringRestrictions, EffectiveStatement<String, TypeStatement.StringRestrictions>> ctx) {
            return new StringRestrictionsEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<String, StringRestrictions,
                EffectiveStatement<String, StringRestrictions>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Override
    public LengthStatement getLength() {
        return firstDeclared(LengthStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends PatternStatement> getPatterns() {
        return allDeclared(PatternStatement.class);
    }

}
