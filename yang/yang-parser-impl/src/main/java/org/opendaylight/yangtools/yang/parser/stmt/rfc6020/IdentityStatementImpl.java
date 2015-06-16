/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class IdentityStatementImpl extends AbstractDeclaredStatement<QName>
        implements IdentityStatement {

    protected IdentityStatementImpl(
            StmtContext<QName, IdentityStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> {

        public Definition() {
            super(Rfc6020Mapping.IDENTITY);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public IdentityStatement createDeclared(
                StmtContext<QName, IdentityStatement, ?> ctx) {
            return new IdentityStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, IdentityStatement> createEffective(
                StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
            return new IdentityEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(final StmtContext.Mutable<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> stmt) throws SourceException {
            stmt.addToNs(IdentityNamespace.class, stmt.getStatementArgument(), stmt);
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
    public BaseStatement getBase() {
        return firstDeclared(BaseStatement.class);
    }

    @Override
    public QName getName() {
        return argument();
    }

}
