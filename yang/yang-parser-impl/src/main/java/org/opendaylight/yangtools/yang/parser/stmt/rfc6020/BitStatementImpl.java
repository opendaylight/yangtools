/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitEffectiveStatementImpl;

public class BitStatementImpl extends AbstractDeclaredStatement<QName> implements BitStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .BIT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.POSITION)
            .build();

    protected BitStatementImpl(final StmtContext<QName, BitStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, BitStatement, EffectiveStatement<QName, BitStatement>> {

        public Definition() {
            super(YangStmtMapping.BIT);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public BitStatement createDeclared(final StmtContext<QName, BitStatement, ?> ctx) {
            return new BitStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, BitStatement> createEffective(
                final StmtContext<QName, BitStatement, EffectiveStatement<QName, BitStatement>> ctx) {
            return new BitEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<QName, BitStatement,
                EffectiveStatement<QName, BitStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public String getName() {
        return argument().getLocalName();
    }

    @Override
    public PositionStatement getPosition() {
        return firstDeclared(PositionStatement.class);
    }

}
