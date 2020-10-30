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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class InstanceIdentifierSpecificationSupport extends BaseStatementSupport<String,
        InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
        .build();

    InstanceIdentifierSpecificationSupport() {
        super(YangStmtMapping.TYPE);
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
        return new RegularInstanceIdentifierSpecification(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected InstanceIdentifierSpecification createEmptyDeclared(
            final StmtContext<String, InstanceIdentifierSpecification, ?> ctx) {
        return new EmptyIdentifierSpecification(ctx.coerceRawStatementArgument());
    }

    @Override
    protected EffectiveStatement<String, InstanceIdentifierSpecification> createEffective(
            final StmtContext<String, InstanceIdentifierSpecification,
                    EffectiveStatement<String, InstanceIdentifierSpecification>> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final EffectiveParentState parent,
            final EffectiveStatementState<String, InstanceIdentifierSpecification> stmt) {
        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(
                BaseTypes.instanceIdentifierType(), ctx.getSchemaPath().get());

        for (EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)subStmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    @Override
    protected EffectiveStatement<String, InstanceIdentifierSpecification> createEmptyEffective(
            final StmtContext<String, InstanceIdentifierSpecification,
                EffectiveStatement<String, InstanceIdentifierSpecification>> ctx, final EffectiveParentState parent,
            final EffectiveStatementState<String, InstanceIdentifierSpecification> stmt) {
        // TODO: we could do better here, but its really splitting hairs
        return createEffective(ctx, ImmutableList.of(), parent, stmt);
    }
}