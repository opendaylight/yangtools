/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator.MAX;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition.Deviate;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeviateEffectiveStatementImpl;

public class DeviateStatementImpl extends AbstractDeclaredStatement<Deviate> implements DeviateStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .DEVIATE)
            .add(Rfc6020Mapping.CONFIG, 0, 1)
            .add(Rfc6020Mapping.DEFAULT, 0, 1)
            .add(Rfc6020Mapping.MANDATORY, 0, 1)
            .add(Rfc6020Mapping.MAX_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MIN_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MUST, 0, MAX)
            .add(Rfc6020Mapping.TYPE, 0, 1)
            .add(Rfc6020Mapping.UNIQUE, 0, MAX)
            .add(Rfc6020Mapping.UNITS, 0, 1)
            .build();

    protected DeviateStatementImpl(StmtContext<Deviate, DeviateStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Deviate, DeviateStatement,
            EffectiveStatement<Deviate, DeviateStatement>> {

        public Definition() {
            super(Rfc6020Mapping.DEVIATE);
        }

        @Override public Deviate parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.parseDeviateFromString(ctx, value);
        }

        @Override public DeviateStatement createDeclared(StmtContext<Deviate, DeviateStatement, ?> ctx) {
            return new DeviateStatementImpl(ctx);
        }

        @Override public EffectiveStatement<Deviate, DeviateStatement> createEffective(
                StmtContext<Deviate, DeviateStatement, EffectiveStatement<Deviate,
                        DeviateStatement>> ctx) {
            return new DeviateEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<Deviate, DeviateStatement,
                EffectiveStatement<Deviate, DeviateStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull @Override
    public Deviate getValue() {
        return argument();
    }
}
