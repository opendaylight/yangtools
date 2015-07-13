/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;

public class Decimal64SpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.Decimal64Specification> implements DecimalTypeDefinition, TypeDefinitionEffectiveBuilder {

    private static final String UNITS = "";
    private static final BigDecimal DEFAULT_VALUE = null;
    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.DECIMAL64);

    private static final String DESCRIPTION = "The decimal64 type represents a subset of the real numbers, which can "
            + "be represented by decimal numerals. The value space of decimal64 is the set of numbers that can "
            + "be obtained by multiplying a 64-bit signed integer by a negative power of ten, i.e., expressible as "
            + "'i x 10^-n' where i is an integer64 and n is an integer between 1 and 18, inclusively.";

    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.3";
    private static final BigDecimal MIN_VALUE = new BigDecimal("-922337203685477580.8");
    private static final BigDecimal MAX_VALUE = new BigDecimal("922337203685477580.7");

    private List<RangeConstraint> rangeConstraints;
    private Integer fractionDigits;
    private SchemaPath path;

    public Decimal64SpecificationEffectiveStatementImpl(StmtContext<String, TypeStatement.Decimal64Specification, EffectiveStatement<String, TypeStatement.Decimal64Specification>> ctx) {
        super(ctx);

        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof FractionDigitsEffectiveStatementImpl) {
                fractionDigits = ((FractionDigitsEffectiveStatementImpl) effectiveStatement).argument();
            }
        }
        
        rangeConstraints = defaultRangeStatements();
    }

    private List<RangeConstraint> defaultRangeStatements() {

        final List<RangeConstraint> rangeStmts = new ArrayList<>();
        final String rangeDescription = "Integer values between " + MIN_VALUE + " and " + MAX_VALUE + ", inclusively.";
        final String rangeReference = RangeConstraintEffectiveImpl.DEFAULT_REFERENCE;

        rangeStmts.add(new RangeConstraintEffectiveImpl(MIN_VALUE, MAX_VALUE, Optional.of(rangeDescription), Optional
                .of(rangeReference)));

        return ImmutableList.copyOf(rangeStmts);
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }

    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public DecimalTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REFERENCE;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((QNAME == null) ? 0 : QNAME.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Decimal64SpecificationEffectiveStatementImpl other = (Decimal64SpecificationEffectiveStatementImpl) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Decimal64SpecificationEffectiveStatementImpl.class.getSimpleName() + "[qName=" + QNAME
                + ", fractionDigits=" + fractionDigits + "]";
    }

    private Decimal64 decimal64Instance = null;

    @Override
    public Decimal64 buildType() {

        if (decimal64Instance != null) {
            return decimal64Instance;
        }
        decimal64Instance = Decimal64.create(path, fractionDigits);

        return decimal64Instance;
    }
}
