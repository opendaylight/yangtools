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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class InstanceIdentifierSpecificationSupport extends AbstractTypeSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(TypeStatement.DEF)
            .addOptional(RequireInstanceStatement.DEF)
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
    protected TypeEffectiveStatement createEffective(final Current<QName, TypeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newInstanceIdentifierBuilder(BaseTypes.instanceIdentifierType(),
            stmt.argumentAsTypeQName());

        // TODO: we could do better here for empty substatements, but its really splitting hairs
        for (var subStmt : substatements) {
            if (subStmt instanceof RequireInstanceEffectiveStatement ries) {
                builder.setRequireInstance(ries.argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }
}
