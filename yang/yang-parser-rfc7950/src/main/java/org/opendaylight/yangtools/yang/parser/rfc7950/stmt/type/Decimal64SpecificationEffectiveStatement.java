/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DecimalTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range.RangeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: hide this class
@VisibleForTesting
public final class Decimal64SpecificationEffectiveStatement
        extends DeclaredEffectiveStatementBase<String, Decimal64Specification>
        implements TypeEffectiveStatement<Decimal64Specification> {

    private final @NonNull DecimalTypeDefinition typeDefinition;

    Decimal64SpecificationEffectiveStatement(
            final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        super(ctx);

        final DecimalTypeBuilder builder = BaseTypes.decimalTypeBuilder(ctx.getSchemaPath().get());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof FractionDigitsEffectiveStatement) {
                builder.setFractionDigits(((FractionDigitsEffectiveStatement) stmt).argument());
            }
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl range = (RangeEffectiveStatementImpl) stmt;
                builder.setRangeConstraint(range, range.argument());
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
