/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class IfFeatureStatementSupport extends AbstractStatementSupport<Predicate<Set<QName>>, IfFeatureStatement,
        EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.IF_FEATURE)
        .build();
    private static final IfFeatureStatementSupport INSTANCE = new IfFeatureStatementSupport();

    private IfFeatureStatementSupport() {
        super(YangStmtMapping.IF_FEATURE);
    }

    public static IfFeatureStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Predicate<Set<QName>> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if (YangVersion.VERSION_1_1.equals(ctx.getRootVersion())) {
            return IfFeaturePredicateVisitor.parseIfFeatureExpression(ctx, value);
        }

        final QName qname = StmtContextUtils.qnameFromArgument(ctx, value);
        return setQNames -> setQNames.contains(qname);
    }

    @Override
    public IfFeatureStatement createDeclared(final StmtContext<Predicate<Set<QName>>, IfFeatureStatement, ?> ctx) {
        return new IfFeatureStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement> createEffective(
            final StmtContext<Predicate<Set<QName>>, IfFeatureStatement,
            EffectiveStatement<Predicate<Set<QName>>, IfFeatureStatement>> ctx) {
        return new IfFeatureEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}