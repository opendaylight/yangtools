/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractStructureStatementSupport
        extends BaseQNameStatementSupport<StructureStatement, StructureEffectiveStatement> {
    AbstractStructureStatementSupport() {
        super(YangDataStructureStatements.STRUCTURE);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected final StructureStatement createDeclared(final StmtContext<QName, StructureStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final StructureStatement createEmptyDeclared(final StmtContext<QName, StructureStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected final StructureEffectiveStatement createEffective(
            final StmtContext<QName, StructureStatement, StructureEffectiveStatement> ctx,
            final StructureStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final StructureEffectiveStatement createEmptyEffective(
            final StmtContext<QName, StructureStatement, StructureEffectiveStatement> ctx,
            final StructureStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
