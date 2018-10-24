/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractContainerStatementSupport
        extends AbstractQNameStatementSupport<ContainerStatement, EffectiveStatement<QName, ContainerStatement>> {

    AbstractContainerStatementSupport() {
        super(YangStmtMapping.CONTAINER);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final void onStatementAdded(
            final Mutable<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public final ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement,?> ctx) {
        return new ContainerStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName,ContainerStatement> createEffective(
            final StmtContext<QName, ContainerStatement, EffectiveStatement<QName,ContainerStatement>> ctx) {
        return new ContainerEffectiveStatementImpl(ctx);
    }
}