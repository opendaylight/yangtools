/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ExtensionStatementSupport
        extends AbstractQNameStatementSupport<ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .EXTENSION)
        .addOptional(YangStmtMapping.ARGUMENT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .build();
    private static final ExtensionStatementSupport INSTANCE = new ExtensionStatementSupport();

    private ExtensionStatementSupport() {
        super(YangStmtMapping.EXTENSION);
    }

    public static ExtensionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public ExtensionStatement createDeclared(final StmtContext<QName, ExtensionStatement,?> ctx) {
        return new ExtensionStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, ExtensionStatement> createEffective(
            final StmtContext<QName, ExtensionStatement, EffectiveStatement<QName,ExtensionStatement>> ctx) {
        return ExtensionEffectiveStatementImpl.create(ctx);
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> stmt) {
        super.onStatementDefinitionDeclared(stmt);

        QName stmtName = stmt.coerceStatementArgument();
        if (OpenConfigStatements.OPENCONFIG_VERSION.getStatementName().isEqualWithoutRevision(stmtName)) {
            stmtName = stmtName.withoutRevision();
        }

        stmt.addContext(ExtensionNamespace.class, stmtName, stmt);

        final StmtContext<QName, ?, ?> argument = StmtContextUtils.findFirstDeclaredSubstatement(stmt,
            ArgumentStatement.class);
        final StmtContext<Boolean, ?, ?> yinElement = StmtContextUtils.findFirstDeclaredSubstatement(stmt,
            YinElementStatement.class);

        stmt.addToNs(StatementDefinitionNamespace.class, stmt.getStatementArgument(),
            new ModelDefinedStatementSupport(new ModelDefinedStatementDefinition(stmt.getStatementArgument(),
                argument != null ? argument.getStatementArgument() : null,
                        yinElement != null ? yinElement.getStatementArgument() : false)));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}