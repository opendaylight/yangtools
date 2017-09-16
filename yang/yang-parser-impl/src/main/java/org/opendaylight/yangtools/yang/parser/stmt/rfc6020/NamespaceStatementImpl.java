/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.net.URI;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.NamespaceEffectiveStatementImpl;

public class NamespaceStatementImpl extends AbstractDeclaredStatement<URI> implements NamespaceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .NAMESPACE)
            .build();

    public static class Definition extends AbstractStatementSupport<URI, NamespaceStatement,
            EffectiveStatement<URI, NamespaceStatement>> {

        public Definition() {
            super(org.opendaylight.yangtools.yang.model.api.YangStmtMapping.NAMESPACE);
        }

        @Override
        public URI parseArgumentValue(final StmtContext<?, ?,?> ctx, final String value) {
            return URI.create(value);
        }

        @Override
        public NamespaceStatement createDeclared(final StmtContext<URI, NamespaceStatement,?> ctx) {
            return new NamespaceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<URI,NamespaceStatement> createEffective(
                final StmtContext<URI, NamespaceStatement, EffectiveStatement<URI, NamespaceStatement>> ctx) {
            return new NamespaceEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    NamespaceStatementImpl(final StmtContext<URI, NamespaceStatement,?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public URI getUri() {
        return argument();
    }
}
