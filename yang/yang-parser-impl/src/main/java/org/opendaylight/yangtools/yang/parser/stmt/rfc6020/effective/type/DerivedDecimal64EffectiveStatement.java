/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class DerivedDecimal64EffectiveStatement extends AbstractTypeEffectiveStatement<Decimal64Specification, DecimalTypeDefinition>
        implements DecimalTypeDefinition {
    private final List<RangeConstraint> rangeConstraints;

    DerivedDecimal64EffectiveStatement(final EffectiveStatement<?, Decimal64Specification> stmt, final SchemaPath path,
            final DecimalTypeDefinition baseType) {
        super(stmt, path, baseType);

        rangeConstraints = calculateRanges(stmt, baseType.getRangeConstraints());
    }

    @Override
    public TypeEffectiveStatement<Decimal64Specification> derive(final EffectiveStatement<?, Decimal64Specification> stmt,
            final SchemaPath path) {
        return new DerivedDecimal64EffectiveStatement(stmt, path, this);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }

    @Override
    public Integer getFractionDigits() {
        // FIXME: does it make sense to override this one?
        return getBaseType().getFractionDigits();
    }
}
