/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.spi.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.spi.type.LeafrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractLeafrefSpecificationSupport extends AbstractStringStatementSupport<LeafrefSpecification,
            EffectiveStatement<String, LeafrefSpecification>> {
    AbstractLeafrefSpecificationSupport() {
        super(YangStmtMapping.TYPE, StatementPolicy.exactReplica());
    }

    @Override
    protected final LeafrefSpecification createDeclared(final StmtContext<String, LeafrefSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new LeafrefSpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected final LeafrefSpecification createEmptyDeclared(final StmtContext<String, LeafrefSpecification, ?> ctx) {
        throw noPath(ctx);
    }

    @Override
    protected EffectiveStatement<String, LeafrefSpecification> createEffective(
            final Current<String, LeafrefSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noPath(stmt);
        }

        final LeafrefTypeBuilder builder = BaseTypes.leafrefTypeBuilder(stmt.argumentAsTypeQName());

        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof PathEffectiveStatement) {
                builder.setPathStatement(((PathEffectiveStatement) subStmt).argument());
            } else if (subStmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)subStmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noPath(final CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.12
         *
         *     When the type is "union", the "type" statement (Section 7.4) MUST be
         *     present.
         */
        return new SourceException("A path statement has to be present", stmt);
    }
}