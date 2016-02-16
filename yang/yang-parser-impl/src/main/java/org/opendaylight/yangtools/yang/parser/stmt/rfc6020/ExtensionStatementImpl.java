/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtensionEffectiveStatementImpl;

public class ExtensionStatementImpl extends AbstractDeclaredStatement<QName> implements ExtensionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .EXTENSION)
            .add(Rfc6020Mapping.ARGUMENT, 0, 1)
            .add(Rfc6020Mapping.DESCRIPTION, 0, 1)
            .add(Rfc6020Mapping.REFERENCE, 0, 1)
            .add(Rfc6020Mapping.STATUS, 0, 1)
            .build();

    protected ExtensionStatementImpl(StmtContext<QName, ExtensionStatement,?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName,ExtensionStatement,EffectiveStatement<QName,ExtensionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.EXTENSION);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?,?,?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public ExtensionStatement createDeclared(StmtContext<QName, ExtensionStatement,?> ctx) {
            return new ExtensionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName,ExtensionStatement> createEffective(StmtContext<QName,ExtensionStatement,EffectiveStatement<QName,ExtensionStatement>> ctx) {
           return new ExtensionEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(final StmtContext.Mutable<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> stmt) {
            stmt.addContext(ExtensionNamespace.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<QName, ExtensionStatement,
                EffectiveStatement<QName, ExtensionStatement>> stmt) {
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
    public ArgumentStatement getArgument() {
        return firstDeclared(ArgumentStatement.class);
    }

}
