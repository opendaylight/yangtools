/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class LeafStatementSupport
        extends AbstractQNameStatementSupport<LeafStatement, EffectiveStatement<QName, LeafStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .LEAF)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final LeafStatementSupport INSTANCE = new LeafStatementSupport();

    private LeafStatementSupport() {
        super(YangStmtMapping.LEAF);
    }

    public static LeafStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx,value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        super.onFullDefinitionDeclared(ctx);
        StmtContextUtils.validateIfFeatureAndWhenOnListKeys(ctx);
    }

    @Override
    public LeafStatement createDeclared(final StmtContext<QName, LeafStatement, ?> ctx) {
        return new LeafStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, LeafStatement> createEffective(
            final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        return new LeafEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}