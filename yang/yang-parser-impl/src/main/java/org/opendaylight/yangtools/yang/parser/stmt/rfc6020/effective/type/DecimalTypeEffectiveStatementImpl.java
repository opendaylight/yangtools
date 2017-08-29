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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DecimalTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(Decimal64SpecificationEffectiveStatementImpl.class);
    private final DecimalTypeDefinition typeDefinition;

    public DecimalTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final DecimalTypeDefinition baseType) {
        super(ctx);

        final RangeRestrictedTypeBuilder<DecimalTypeDefinition> builder =
                RestrictedTypes.newDecima64Builder(baseType, TypeUtils.typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl range = (RangeEffectiveStatementImpl) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
            if (stmt instanceof FractionDigitsEffectiveStatementImpl) {
                final Integer digits = ((FractionDigitsEffectiveStatementImpl)stmt).argument();

                if (!baseType.getFractionDigits().equals(digits)) {
                    LOG.warn("Ignoring attempt to override fraction-digits to {} at {}, base type is {}", digits,
                        ctx.getStatementSourceReference(), baseType);

                    // FIXME: promote to a full error once our models are fixed
                    // throw new SourceException(String.format("Cannot override fraction-digits from base type %s",
                    // baseType), ctx.getStatementSourceReference());
                }
            }
        }

        typeDefinition = builder.build();
    }

    @Nonnull
    @Override
    public DecimalTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
