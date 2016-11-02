/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IfFeatureEffectiveStatementImpl;

public class IfFeatureStatementImpl extends AbstractDeclaredStatement<QName>
        implements IfFeatureStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .IF_FEATURE)
            .build();

    protected IfFeatureStatementImpl(
            StmtContext<QName, IfFeatureStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, IfFeatureStatement, EffectiveStatement<QName, IfFeatureStatement>> {

        public Definition() {
            super(Rfc6020Mapping.IF_FEATURE);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public IfFeatureStatement createDeclared(
                StmtContext<QName, IfFeatureStatement, ?> ctx) {
            return new IfFeatureStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, IfFeatureStatement> createEffective(
                StmtContext<QName, IfFeatureStatement, EffectiveStatement<QName, IfFeatureStatement>> ctx) {
            return new IfFeatureEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<QName, IfFeatureStatement,
                EffectiveStatement<QName, IfFeatureStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

}
