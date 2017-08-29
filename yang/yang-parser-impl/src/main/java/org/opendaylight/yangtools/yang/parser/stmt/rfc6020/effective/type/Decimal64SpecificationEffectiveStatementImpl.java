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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DecimalTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class Decimal64SpecificationEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String, Decimal64Specification>
        implements TypeEffectiveStatement<Decimal64Specification> {

    private final DecimalTypeDefinition typeDefinition;

    public Decimal64SpecificationEffectiveStatementImpl(
            final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        super(ctx);

        final DecimalTypeBuilder builder = BaseTypes.decimalTypeBuilder(ctx.getSchemaPath().get());

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof FractionDigitsEffectiveStatementImpl) {
                builder.setFractionDigits(((FractionDigitsEffectiveStatementImpl) stmt).argument());
            }
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl range = (RangeEffectiveStatementImpl) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
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
