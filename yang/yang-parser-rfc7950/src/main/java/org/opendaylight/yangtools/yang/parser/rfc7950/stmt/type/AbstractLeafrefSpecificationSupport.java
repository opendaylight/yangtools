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
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.LeafrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractLeafrefSpecificationSupport
        extends BaseStatementSupport<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> {
    AbstractLeafrefSpecificationSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    protected final LeafrefSpecification createDeclared(final StmtContext<String, LeafrefSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new LeafrefSpecificationImpl(ctx, substatements);
    }

    @Override
    protected final LeafrefSpecification createEmptyDeclared(final StmtContext<String, LeafrefSpecification, ?> ctx) {
        throw noPath(ctx);
    }

    @Override
    protected final EffectiveStatement<String, LeafrefSpecification> createEffective(
            final StmtContext<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> ctx,
            final LeafrefSpecification declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final LeafrefTypeBuilder builder = BaseTypes.leafrefTypeBuilder(ctx.getSchemaPath().get());

        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof PathEffectiveStatement) {
                builder.setPathStatement(((PathEffectiveStatement) stmt).argument());
            } else if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)stmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    @Override
    protected final EffectiveStatement<String, LeafrefSpecification> createEmptyEffective(
            final StmtContext<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> ctx,
            final LeafrefSpecification declared) {
        throw noPath(ctx);
    }

    private static SourceException noPath(final StmtContext<?, ?, ?> ctx) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.12
         *
         *     When the type is "union", the "type" statement (Section 7.4) MUST be
         *     present.
         */
        return new SourceException("A path statement has to be present", ctx.getStatementSourceReference());
    }
}