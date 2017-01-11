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
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IfFeatureEffectiveStatementImpl;

public class IfFeatureStatementImpl extends AbstractDeclaredStatement<QName>
        implements IfFeatureStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .IF_FEATURE)
            .build();

    protected IfFeatureStatementImpl(
            final StmtContext<QName, IfFeatureStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, IfFeatureStatement, EffectiveStatement<QName, IfFeatureStatement>> {

        public Definition() {
            super(YangStmtMapping.IF_FEATURE);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public IfFeatureStatement createDeclared(
                final StmtContext<QName, IfFeatureStatement, ?> ctx) {
            return new IfFeatureStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, IfFeatureStatement> createEffective(
                final StmtContext<QName, IfFeatureStatement, EffectiveStatement<QName, IfFeatureStatement>> ctx) {
            return new IfFeatureEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<QName, IfFeatureStatement, EffectiveStatement<QName, IfFeatureStatement>> ctx) {
//            final Mutable<?, ?, ?> possibleValueCtx = ctx.getParentContext();
//            final Mutable<?, ?, ?> possibleTypeCtx = possibleValueCtx.getParentContext();
//            if (isEnumOrBitCtx(possibleValueCtx) && isTypeCtx(possibleTypeCtx)) {
//                final Object argument = possibleTypeCtx.getStatementArgument();
//                SourceException.throwIf(!(argument instanceof QName), possibleTypeCtx.getStatementSourceReference(),
//                        "Argument %s of type statement is not QName.", argument);
//                final QName typeQName = (QName) argument;
//                Set<String> valuesWithIfFeature = possibleValueCtx.getFromNamespace(
//                        TypeValuesWithIfFeatureNamespace.class, typeQName);
//                if (valuesWithIfFeature == null) {
//                    valuesWithIfFeature = new HashSet<>();
//                    possibleValueCtx.addToNs(TypeValuesWithIfFeatureNamespace.class, typeQName, valuesWithIfFeature);
//                }
//                valuesWithIfFeature.add(possibleValueCtx.rawStatementArgument());
//            }
        }

        private static boolean isTypeCtx(final Mutable<?, ?, ?> ctx) {
            return ctx != null && ctx.getPublicDefinition().equals(YangStmtMapping.TYPE);
        }

        private static boolean isEnumOrBitCtx(final StmtContext<?, ?, ?> ctx) {
            return ctx.getRoot().getRootVersion() == YangVersion.VERSION_1_1
                    && (ctx.getPublicDefinition().equals(YangStmtMapping.ENUM) || ctx.getPublicDefinition().equals(
                            YangStmtMapping.BIT));
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

}
