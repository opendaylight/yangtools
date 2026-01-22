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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class LeafrefSpecificationSupport extends AbstractTypeSupport<LeafrefSpecification> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.TYPE).addMandatory(PathStatement.DEFINITION).build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.TYPE)
            .addMandatory(PathStatement.DEFINITION)
            .addOptional(RequireInstanceStatement.DEFINITION)
            .build();

    private LeafrefSpecificationSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(config, validator);
    }

    static LeafrefSpecificationSupport rfc6020Instance(final YangParserConfiguration config) {
        return new LeafrefSpecificationSupport(config, RFC6020_VALIDATOR);
    }

    static LeafrefSpecificationSupport rfc7950Instance(final YangParserConfiguration config) {
        return new LeafrefSpecificationSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected LeafrefSpecification createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            throw noPath(ctx);
        }
        return new LeafrefSpecificationImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected LeafrefSpecification attachDeclarationReference(final LeafrefSpecification stmt,
            final DeclarationReference reference) {
        return new RefLeafrefSpecification(stmt, reference);
    }

    @Override
    protected EffectiveStatement<QName, LeafrefSpecification> createEffective(
            final Current<QName, LeafrefSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noPath(stmt);
        }

        final var builder = BaseTypes.leafrefTypeBuilder(stmt.argumentAsTypeQName());

        for (var subStmt : substatements) {
            switch (subStmt) {
                case PathEffectiveStatement path -> builder.setPathStatement(path.argument());
                case RequireInstanceEffectiveStatement ries -> builder.setRequireInstance(ries.argument());
                case null, default -> {
                    // No-op
                }
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noPath(final CommonStmtCtx stmt) {
        /*
         *  https://www.rfc-editor.org/rfc/rfc7950#section-9.12
         *
         *     When the type is "union", the "type" statement (Section 7.4) MUST be
         *     present.
         */
        return new SourceException("A path statement has to be present", stmt);
    }
}
