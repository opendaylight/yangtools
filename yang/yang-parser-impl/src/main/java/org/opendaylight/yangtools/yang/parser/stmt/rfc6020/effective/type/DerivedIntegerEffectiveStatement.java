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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

final class DerivedIntegerEffectiveStatement extends AbstractTypeEffectiveStatement<TypeStatement, IntegerTypeDefinition>
        implements IntegerTypeDefinition {
    private final List<RangeConstraint> rangeConstraints;

    DerivedIntegerEffectiveStatement(final EffectiveStatement<?, TypeStatement> stmt, final SchemaPath path,
            final IntegerTypeDefinition baseType) {
        super(stmt, path, baseType);
        this.rangeConstraints = calculateRanges(stmt, baseType.getRangeConstraints());
    }

    @Override
    public TypeEffectiveStatement<TypeStatement> derive(final EffectiveStatement<?, TypeStatement> stmt, final SchemaPath path) {
        return new DerivedIntegerEffectiveStatement(stmt, path, this);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }
}