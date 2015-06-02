/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToDerivedType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DefaultEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeEffectiveStatementImpl;

public class DerivedType extends EffectiveStatementBase<QName, TypedefStatement> implements YangBaseType {

    private final QName qName;
    private final SchemaPath path;
    private final YangBaseType baseType;

    private final List<RangeConstraint> rangeConstraints;
    private final List<LengthConstraint> lengthConstraints;
    private final List<PatternConstraint> patternConstraints;
    private final List<UnknownSchemaNode> unknownSchemaNodes;

    private final Integer fractionDigits;

    private final String units;
    private final Object defaultValue;

    private final String description;
    private final String reference;
    private final Status status;

    // public DerivedType(StmtContext<QName, TypedefStatement,
    // EffectiveStatement<QName, TypedefStatement>> ctx) {
    // super(ctx);
    //
    // qName = ctx.getStatementArgument();
    // path = Utils.getSchemaPath(ctx);
    //
    // baseType = initBaseType();
    //
    // rangeConstraints = initRanges();
    // lengthConstraints = initLengths();
    // patternConstraints = initPatterns();
    //
    // fractionDigits = initFractionDigits();
    //
    // description = initDescription();
    // reference = initReference();
    // status = initStatus();
    // units = initUnits();
    // defaultValue = initDefaultValue();
    // }

    public DerivedType(StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx,
            DerivedType other) {
        super(ctx);

        this.qName = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);

        this.baseType = initBaseType(ctx);

        this.rangeConstraints = initRanges();
        this.lengthConstraints = initLengths();
        this.patternConstraints = initPatterns();
        this.unknownSchemaNodes = initUnknownStatements();

        this.fractionDigits = initFractionDigits();

        this.description = initDescription();
        this.reference = initReference();
        this.status = initStatus();
        this.units = initUnits();
        this.defaultValue = initDefaultValue();
    }

    private YangBaseType initBaseType(final StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx) {
        final TypeDefinition typeStmt = firstSubstatementOfType(TypeDefinition.class);

        if (typeStmt == null) {
            throw new IllegalStateException("none type defined for node " + typeStmt.getQName());
        }

        final DerivedType derivedType = ctx.getFromNamespace(QNameToDerivedType.class, typeStmt.getQName());

        if (derivedType == null) {
            return TypeUtils.getYangBaseTypeFromString(typeStmt.getQName().getLocalName());
        }
        else {
            return derivedType;
        }
    }

    private List<RangeConstraint> initRanges() {
        final RangeEffectiveStatementImpl rangeConstraints = firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraints != null ? rangeConstraints.argument() : baseType.getRangeConstraints();
    }

    private List<LengthConstraint> initLengths() {
        final LengthEffectiveStatementImpl lengthConstraints = firstEffective(LengthEffectiveStatementImpl.class);
        return lengthConstraints != null ? lengthConstraints.argument() : baseType.getLengthConstraints();
    }

    private List<PatternConstraint> initPatterns() {
        final List<PatternConstraint> patternConstraints = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PatternEffectiveStatementImpl) {
                patternConstraints.add(((PatternEffectiveStatementImpl) effectiveStatement).argument());
            }
        }

        return !patternConstraints.isEmpty() ? ImmutableList.copyOf(patternConstraints) : baseType
                .getPatternConstraints();
    }

    private List<UnknownSchemaNode> initUnknownStatements() {

        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownEffectiveStatementImpl) {
                unknownSchemaNodes.add(((UnknownEffectiveStatementImpl) effectiveStatement));
            }
        }

        return !unknownSchemaNodes.isEmpty() ? ImmutableList.copyOf(unknownSchemaNodes) : baseType
                .getUnknownSchemaNodes();
    }

    private Integer initFractionDigits() {
        final FractionDigitsEffectiveStatementImpl fractionDigitsEffStmt = firstEffective(FractionDigitsEffectiveStatementImpl.class);
        return fractionDigitsEffStmt != null ? fractionDigitsEffStmt.argument() : baseType.getFractionDigits();
    }

    private String initDescription() {
        final DescriptionEffectiveStatementImpl descEffectiveStatement = firstEffective(DescriptionEffectiveStatementImpl.class);
        return descEffectiveStatement != null ? descEffectiveStatement.argument() : baseType.getDescription();
    }

    private String initReference() {
        final ReferenceEffectiveStatementImpl refEffectiveStatement = firstEffective(ReferenceEffectiveStatementImpl.class);
        return refEffectiveStatement != null ? refEffectiveStatement.argument() : baseType.getReference();
    }

    private Status initStatus() {
        final StatusEffectiveStatementImpl statusEffectiveStatement = firstEffective(StatusEffectiveStatementImpl.class);
        return statusEffectiveStatement != null ? statusEffectiveStatement.argument() : baseType.getStatus();
    }

    private String initUnits() {
        final UnitsEffectiveStatementImpl unitsEffectiveStatement = firstEffective(UnitsEffectiveStatementImpl.class);
        return unitsEffectiveStatement != null ? unitsEffectiveStatement.argument() : baseType.getUnits();
    }

    private Object initDefaultValue() {
        final DefaultEffectiveStatementImpl defaultEffectiveStatement = firstEffective(DefaultEffectiveStatementImpl.class);
        return defaultEffectiveStatement != null ? defaultEffectiveStatement.argument() : baseType.getDefaultValue();
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeConstraints;
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public TypeDefinition<?> getBaseType() {
        return baseType;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
