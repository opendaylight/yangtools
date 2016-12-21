/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IfFeatureEffectiveStatementImpl;

public class IfFeatureStatementImpl extends AbstractDeclaredStatement<Predicate<Set<QName>>>
        implements IfFeatureStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .IF_FEATURE)
            .build();

    protected IfFeatureStatementImpl(
            final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Predicate<Set<QName>>, IfFeatureStatement, EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> {

        public Definition() {
            super(YangStmtMapping.IF_FEATURE);
        }

        @Override
        public Predicate<Set<QName>> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            if(YangVersion.VERSION_1_1.equals(ctx.getRootVersion())) {
                return Utils.parseIfFeatureExpression(ctx, value);
            } else {
                return setQNames -> setQNames.contains(Utils.qNameFromArgument(ctx, value));
            }
        }

        @Override
        public IfFeatureStatement createDeclared(
                final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, ?> ctx) {
            return new IfFeatureStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement> createEffective(
                final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> ctx) {
            return new IfFeatureEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<Predicate<Set<QName>>, IfFeatureStatement,
                EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            getSubstatementValidator().validate(stmt);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return ;
    }
}
