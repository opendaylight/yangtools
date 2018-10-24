/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnydataStatementSupport extends
        AbstractQNameStatementSupport<AnydataStatement, EffectiveStatement<QName, AnydataStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ANYDATA)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final AnydataStatementSupport INSTANCE = new AnydataStatementSupport();

    private AnydataStatementSupport() {
        super(YangStmtMapping.ANYDATA);
    }

    public static AnydataStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, AnydataStatement,
            EffectiveStatement<QName, AnydataStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public AnydataStatement createDeclared(final StmtContext<QName, AnydataStatement, ?> ctx) {
        return new AnydataStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, AnydataStatement> createEffective(
            final StmtContext<QName, AnydataStatement, EffectiveStatement<QName, AnydataStatement>> ctx) {
        return new AnydataEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}