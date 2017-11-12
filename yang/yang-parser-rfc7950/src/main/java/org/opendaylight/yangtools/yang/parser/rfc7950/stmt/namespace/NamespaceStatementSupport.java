/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.namespace;

import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class NamespaceStatementSupport extends AbstractStatementSupport<URI, NamespaceStatement,
        EffectiveStatement<URI, NamespaceStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .NAMESPACE)
        .build();

    public NamespaceStatementSupport() {
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