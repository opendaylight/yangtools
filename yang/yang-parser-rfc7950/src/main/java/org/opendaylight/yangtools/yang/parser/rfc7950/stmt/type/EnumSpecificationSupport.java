/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class EnumSpecificationSupport
        extends AbstractStatementSupport<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.TYPE).addMultiple(YangStmtMapping.ENUM).build();

    EnumSpecificationSupport() {
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
    protected EnumSpecification createDeclared(final StmtContext<String, EnumSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new EnumSpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected EnumSpecification createEmptyDeclared(final StmtContext<String, EnumSpecification, ?> ctx) {
        throw noEnum(ctx);
    }

    @Override
    protected EffectiveStatement<String, EnumSpecification> createEffective(
            final Current<String, EnumSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noEnum(stmt);
        }

        final EnumerationTypeBuilder builder = BaseTypes.enumerationTypeBuilder(stmt.wrapSchemaPath());
        Integer highestValue = null;
        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof EnumEffectiveStatement) {
                final EnumEffectiveStatement enumSubStmt = (EnumEffectiveStatement) subStmt;

                final Optional<Integer> declaredValue =
                        enumSubStmt.findFirstEffectiveSubstatementArgument(ValueEffectiveStatement.class);
                final int effectiveValue;
                if (declaredValue.isEmpty()) {
                    if (highestValue != null) {
                        SourceException.throwIf(highestValue == 2147483647, stmt,
                            "Enum '%s' must have a value statement", enumSubStmt);
                        effectiveValue = highestValue + 1;
                    } else {
                        effectiveValue = 0;
                    }
                } else {
                    effectiveValue = declaredValue.orElseThrow();
                }

                final EnumPair pair = EffectiveTypeUtil.buildEnumPair(enumSubStmt, effectiveValue);
                if (highestValue == null || highestValue < pair.getValue()) {
                    highestValue = pair.getValue();
                }

                builder.addEnum(pair);
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noEnum(final CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.6.4
         *
         *     The "enum" statement, which is a substatement to the "type"
         *     statement, MUST be present if the type is "enumeration".
         */
        return new SourceException("At least one enum statement has to be present", stmt);
    }

}