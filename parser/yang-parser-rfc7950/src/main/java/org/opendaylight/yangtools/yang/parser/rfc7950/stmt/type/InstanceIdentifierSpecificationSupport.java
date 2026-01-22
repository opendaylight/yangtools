/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.InstanceIdentifierTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class InstanceIdentifierSpecificationSupport
        extends AbstractTypeSupport<InstanceIdentifierSpecification> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(TypeStatement.DEFINITION)
            .addOptional(RequireInstanceStatement.DEFINITION)
            .build();

    InstanceIdentifierSpecificationSupport(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected InstanceIdentifierSpecification createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyIdentifierSpecification(ctx.getRawArgument(), ctx.getArgument())
            : new RegularInstanceIdentifierSpecification(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected InstanceIdentifierSpecification attachDeclarationReference(final InstanceIdentifierSpecification stmt,
            final DeclarationReference reference) {
        return new RefInstanceIdentifierSpecification(stmt, reference);
    }

    @Override
    protected EffectiveStatement<QName, InstanceIdentifierSpecification> createEffective(
            final Current<QName, InstanceIdentifierSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(
            BaseTypes.instanceIdentifierType(), stmt.argumentAsTypeQName());

        // TODO: we could do better here for empty substatements, but its really splitting hairs
        for (EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)subStmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }
}
