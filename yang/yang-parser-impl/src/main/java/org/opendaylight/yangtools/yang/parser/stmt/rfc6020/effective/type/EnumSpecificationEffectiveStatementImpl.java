/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class EnumSpecificationEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String, EnumSpecification> implements
        TypeEffectiveStatement<EnumSpecification> {

    private final EnumTypeDefinition typeDefinition;

    public EnumSpecificationEffectiveStatementImpl(
            final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);

        final EnumerationTypeBuilder builder = BaseTypes.enumerationTypeBuilder(ctx.getSchemaPath().get());
        Integer highestValue = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof EnumEffectiveStatementImpl) {
                final EnumEffectiveStatementImpl enumSubStmt = (EnumEffectiveStatementImpl) stmt;

                final int effectiveValue;
                if (enumSubStmt.getDeclaredValue() == null) {
                    if (highestValue != null) {
                        SourceException.throwIf(highestValue == 2147483647, ctx.getStatementSourceReference(),
                                "Enum '%s' must have a value statement", enumSubStmt);
                        effectiveValue = highestValue + 1;
                    } else {
                        effectiveValue = 0;
                    }
                } else {
                    effectiveValue = enumSubStmt.getDeclaredValue();
                }

                final EnumPair p = EnumPairBuilder.create(enumSubStmt.getName(), effectiveValue)
                        .setDescription(enumSubStmt.getDescription()).setReference(enumSubStmt.getReference())
                        .setStatus(enumSubStmt.getStatus()).setUnknownSchemaNodes(enumSubStmt.getUnknownSchemaNodes())
                        .build();

                if (highestValue == null || highestValue < p.getValue()) {
                    highestValue = p.getValue();
                }

                builder.addEnum(p);
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl) stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Nonnull
    @Override
    public EnumTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
