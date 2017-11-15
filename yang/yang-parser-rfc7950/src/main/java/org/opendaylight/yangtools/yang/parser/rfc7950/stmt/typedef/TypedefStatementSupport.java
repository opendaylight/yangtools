/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class TypedefStatementSupport extends
        AbstractQNameStatementSupport<TypedefStatement, EffectiveStatement<QName, TypedefStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPEDEF)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .build();
    private static final TypedefStatementSupport INSTANCE = new TypedefStatementSupport();

    private TypedefStatementSupport() {
        super(YangStmtMapping.TYPEDEF);
    }

    public static TypedefStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public TypedefStatement createDeclared(final StmtContext<QName, TypedefStatement, ?> ctx) {
        return new TypedefStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, TypedefStatement> createEffective(
            final StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx) {
        return new TypedefEffectiveStatementImpl(ctx);
    }

    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<QName, TypedefStatement,
            EffectiveStatement<QName, TypedefStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (stmt != null && stmt.getParentContext() != null) {
            final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> existing = stmt.getParentContext()
                    .getFromNamespace(TypeNamespace.class, stmt.getStatementArgument());
            SourceException.throwIf(existing != null, stmt.getStatementSourceReference(),
                    "Duplicate name for typedef %s", stmt.getStatementArgument());

            stmt.getParentContext().addContext(TypeNamespace.class, stmt.getStatementArgument(), stmt);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}