/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

class RangeTypeDefinitionBuilder<T extends TypeDefinition<T>>
        extends AbstractConstrainedTypeDefinitionBuilder<T> {

    @Override
    protected void addEffectiveStatement(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof RangeEffectiveStatementImpl) {
            getConstraints().addRanges(((RangeEffectiveStatementImpl)stmt).argument());
        }
    }

    @Override
    protected void modifyBuilder(final Builder builder) {
        final TypeConstraints c = validConstraints();

        c.getRange();
        final Integer f = c.getFractionDigits();
        Preconditions.checkArgument(f != null, "Fraction digits not defined");
        builder.ranges(getRangeConstraints());
    }

//    private static boolean validateRanges(final List<RangeConstraint> initRanges) {
//        for (RangeConstraint rangeConstraint : initRanges) {
//
//            String maxValueString = rangeConstraint.getMax().toString();
//            String minValueString = rangeConstraint.getMin().toString();
//
//            if ((!"max".equals(maxValueString) && MAX_VALUE.compareTo(new BigDecimal(maxValueString)) < 0)
//                    || (!"min".equals(minValueString) && MIN_VALUE.compareTo(new BigDecimal(minValueString)) > 0)) {
//                return false;
//            }
//        }
//        return true;
//    }

    protected final List<RangeConstraint> getRangeConstraints() {
        return validConstraints().getRange();
    }
}
