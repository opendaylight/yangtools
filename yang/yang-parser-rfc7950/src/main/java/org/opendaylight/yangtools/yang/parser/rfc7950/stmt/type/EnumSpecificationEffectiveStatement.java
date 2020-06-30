/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class EnumSpecificationEffectiveStatement extends DeclaredEffectiveStatementBase<String, EnumSpecification>
        implements TypeEffectiveStatement<EnumSpecification> {

    private final @NonNull EnumTypeDefinition typeDefinition;

    EnumSpecificationEffectiveStatement(
            final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);

        final EnumerationTypeBuilder builder = BaseTypes.enumerationTypeBuilder(ctx.getSchemaPath().get());
        Integer highestValue = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof EnumEffectiveStatement) {
                final EnumEffectiveStatement enumSubStmt = (EnumEffectiveStatement) stmt;

                final Optional<Integer> declaredValue =
                        enumSubStmt.findFirstEffectiveSubstatementArgument(ValueEffectiveStatement.class);
                final int effectiveValue;
                if (declaredValue.isEmpty()) {
                    if (highestValue != null) {
                        SourceException.throwIf(highestValue == 2147483647, ctx.getStatementSourceReference(),
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
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode) stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public EnumTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
