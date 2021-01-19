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
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.InstanceIdentifierTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class InstanceIdentifierSpecificationSupport extends AbstractStatementSupport<String,
        InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
        .build();

    InstanceIdentifierSpecificationSupport() {
        super(YangStmtMapping.TYPE, CopyPolicy.DECLARED_COPY);
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
    protected InstanceIdentifierSpecification createDeclared(
            final StmtContext<String, InstanceIdentifierSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularInstanceIdentifierSpecification(ctx.getRawArgument(), substatements);
    }

    @Override
    protected InstanceIdentifierSpecification createEmptyDeclared(
            final StmtContext<String, InstanceIdentifierSpecification, ?> ctx) {
        return new EmptyIdentifierSpecification(ctx.getRawArgument());
    }

    @Override
    protected EffectiveStatement<String, InstanceIdentifierSpecification> createEffective(
            final Current<String, InstanceIdentifierSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(
            BaseTypes.instanceIdentifierType(), stmt.wrapSchemaPath());

        // TODO: we could do better here for empty substatements, but its really splitting hairs
        for (EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)subStmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }
}