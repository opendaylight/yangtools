/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;

public class Decimal64SpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, Decimal64Specification>
        implements TypeEffectiveStatement<Decimal64Specification> {

    public Decimal64SpecificationEffectiveStatementImpl(
            final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        super(ctx);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof FractionDigitsEffectiveStatementImpl) {
                fractionDigits = ((FractionDigitsEffectiveStatementImpl) effectiveStatement)
                        .argument();
            }
        }

        List<RangeConstraint> initRanges = initRanges();

        if (!initRanges.isEmpty() && validateRanges(initRanges)) {
            isExtended = true;
            rangeConstraints = ImmutableList.copyOf(initRanges);
            SchemaPath parentPath = Utils.getSchemaPath(ctx.getParentContext());
            extendedTypeQName = QName.create(parentPath.getLastComponent().getModule(), QNAME.getLocalName());
            path = parentPath.createChild(extendedTypeQName);
        } else {
            isExtended = false;
            rangeConstraints = DEFAULT_RANGE_STATEMENTS;
            path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);
        }
    }

    private static boolean validateRanges(final List<RangeConstraint> initRanges) {
        for (RangeConstraint rangeConstraint : initRanges) {

            String maxValueString = rangeConstraint.getMax().toString();
            String minValueString = rangeConstraint.getMin().toString();

            if ((!"max".equals(maxValueString) && MAX_VALUE.compareTo(new BigDecimal(maxValueString)) < 0)
                    || (!"min".equals(minValueString) && MIN_VALUE.compareTo(new BigDecimal(minValueString)) > 0)) {
                return false;
            }
        }
        return true;
    }

    private List<RangeConstraint> initRanges() {
        final RangeEffectiveStatementImpl rangeConstraintsStmt = firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraintsStmt != null ? rangeConstraintsStmt.argument() : Collections.<RangeConstraint> emptyList();
    }

    @Override
    public DecimalTypeDefinition getTypeDefinition() {
        // TODO Auto-generated method stub
        return null;
    }
}
