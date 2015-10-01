/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

public class Decimal64SpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, Decimal64Specification>
        implements DecimalTypeDefinition, TypeDefinitionEffectiveBuilder,
        DefinitionAwareTypeEffectiveStatement<Decimal64Specification, DecimalTypeDefinition> {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.DECIMAL64);
    private static final BigDecimal MIN_VALUE = new BigDecimal("-922337203685477580.8");
    private static final BigDecimal MAX_VALUE = new BigDecimal("922337203685477580.7");

    private final Decimal64 type;
    private final List<RangeConstraint> rangeConstraints;

    public Decimal64SpecificationEffectiveStatementImpl(
            final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        super(ctx);

        List<RangeConstraint> ranges = Collections.emptyList();
        Integer fractionDigits = null;
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof FractionDigitsEffectiveStatementImpl) {
                fractionDigits = ((FractionDigitsEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof RangeEffectiveStatementImpl) {
                ranges = ((RangeEffectiveStatementImpl)effectiveStatement).argument();
            }
        }
        Preconditions.checkArgument(fractionDigits != null, "No fraction digits statement found");

        type = Decimal64.create(Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME), fractionDigits);
        rangeConstraints = calculateRanges(ranges, type.getRangeConstraints());
    }

    private static List<RangeConstraint> calculateRanges(final List<RangeConstraint> ranges,
            final List<RangeConstraint> base) {
        if (!validateRanges(ranges)) {
            // FIXME: this is not nice
            throw new IllegalArgumentException();
        }

        if (!ranges.isEmpty()) {
            // FIXME: get source reference
            final TypeConstraints constraints = new TypeConstraints("foo", 4);
            constraints.addRanges(base);
            constraints.addRanges(ranges);
            constraints.validateConstraints();
            return constraints.getRange();
        } else {
            return base;
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

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }

    @Override
    public Integer getFractionDigits() {
        return type.getFractionDigits();
    }

    @Override
    public DecimalTypeDefinition getBaseType() {
        return type.getBaseType();
    }

    @Override
    public String getUnits() {
        return type.getUnits();
    }

    @Override
    public Object getDefaultValue() {
        return type.getDefaultValue();
    }

    @Override
    public QName getQName() {
        return type.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return type.getPath();
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return type.getDescription();
    }

    @Override
    public String getReference() {
        return type.getReference();
    }

    @Override
    public Status getStatus() {
        return type.getStatus();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public DecimalTypeDefinition buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<Decimal64Specification> derive(final EffectiveStatement<?, Decimal64Specification> stmt,
            final SchemaPath path) {
        return new DerivedDecimal64EffectiveStatement(stmt, path, this);
    }
}
