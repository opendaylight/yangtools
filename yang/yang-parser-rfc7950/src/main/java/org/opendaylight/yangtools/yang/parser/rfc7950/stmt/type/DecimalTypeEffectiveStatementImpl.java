/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import java.math.BigDecimal;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range.RangeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class DecimalTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {
    private final @NonNull DecimalTypeDefinition typeDefinition;

    DecimalTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final DecimalTypeDefinition baseType) {
        super(ctx);

        final RangeRestrictedTypeBuilder<DecimalTypeDefinition, BigDecimal> builder =
                RestrictedTypes.newDecima64Builder(baseType, AbstractTypeStatementSupport.typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl range = (RangeEffectiveStatementImpl) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof FractionDigitsEffectiveStatement) {
                final Integer digits = ((FractionDigitsEffectiveStatement)stmt).argument();
                SourceException.throwIf(baseType.getFractionDigits() != digits, ctx.getStatementSourceReference(),
                    "Cannot override fraction-digits from base type %s to %s", baseType, digits);
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public DecimalTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
