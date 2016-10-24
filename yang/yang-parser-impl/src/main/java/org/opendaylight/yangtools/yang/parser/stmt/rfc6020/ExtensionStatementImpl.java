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
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtensionEffectiveStatementImpl;

public class ExtensionStatementImpl extends AbstractDeclaredStatement<QName> implements ExtensionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .EXTENSION)
            .addOptional(Rfc6020Mapping.ARGUMENT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .build();

    protected ExtensionStatementImpl(final StmtContext<QName, ExtensionStatement,?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName,ExtensionStatement,EffectiveStatement<QName,ExtensionStatement>> {
        public Definition() {
            super(Rfc6020Mapping.EXTENSION);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public ExtensionStatement createDeclared(final StmtContext<QName, ExtensionStatement,?> ctx) {
            return new ExtensionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName,ExtensionStatement> createEffective(
                final StmtContext<QName,ExtensionStatement ,EffectiveStatement<QName,ExtensionStatement>> ctx) {

            // Look at the thread-local leak in case we are invoked recursively
            final ExtensionEffectiveStatementImpl existing = RecursiveObjectLeaker.lookup(ctx,
                ExtensionEffectiveStatementImpl.class);
            if (existing != null) {
                // Careful! this not fully initialized!
                return existing;
            }

            RecursiveObjectLeaker.beforeConstructor(ctx);
            try {
                // This result is fine, we know it has been completely initialized
                return new ExtensionEffectiveStatementImpl(ctx);
            } finally {
                RecursiveObjectLeaker.afterConstructor(ctx);
            }
        }

        @Override
        public void onStatementDefinitionDeclared(final StmtContext.Mutable<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> stmt) {
            super.onStatementDefinitionDeclared(stmt);

            stmt.addContext(ExtensionNamespace.class, stmt.getStatementArgument(), stmt);

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
        public void onFullDefinitionDeclared(final StmtContext.Mutable<QName, ExtensionStatement,
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
