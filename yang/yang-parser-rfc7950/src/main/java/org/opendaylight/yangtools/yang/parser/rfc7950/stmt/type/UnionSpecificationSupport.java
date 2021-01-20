/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.UnionSpecification;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class UnionSpecificationSupport
        extends AbstractStatementSupport<String, UnionSpecification, EffectiveStatement<String, UnionSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .TYPE)
        .addMultiple(YangStmtMapping.TYPE)
        .build();

    UnionSpecificationSupport() {
        super(YangStmtMapping.TYPE, StatementPolicy.legacyDeclaredCopy());
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected UnionSpecification createDeclared(final StmtContext<String, UnionSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new UnionSpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected UnionSpecification createEmptyDeclared(final StmtContext<String, UnionSpecification, ?> ctx) {
        throw noType(ctx);
    }

    @Override
    protected EffectiveStatement<String, UnionSpecification> createEffective(
            final Current<String, UnionSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noType(stmt);
        }

        final UnionTypeBuilder builder = BaseTypes.unionTypeBuilder(stmt.wrapSchemaPath());

        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof TypeEffectiveStatement) {
                builder.addType(((TypeEffectiveStatement<?>)subStmt).getTypeDefinition());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noType(final @NonNull CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.12
         *
         *     When the type is "union", the "type" statement (Section 7.4) MUST be
         *     present.
         */
        return new SourceException("At least one type statement has to be present", stmt);
    }
}
