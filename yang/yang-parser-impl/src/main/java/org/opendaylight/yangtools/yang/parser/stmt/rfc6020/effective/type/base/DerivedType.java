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
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeEffectiveStatementImpl;

public class DerivedType extends EffectiveStatementBase<QName, TypedefStatement> implements YangBaseType {

    protected final QName qName;
    protected final SchemaPath path;
    protected YangBaseType baseType;

    protected List<RangeConstraint> rangeConstraints;
    protected List<LengthConstraint> lengthConstraints;
    protected List<PatternConstraint> patternConstraints;
    protected List<UnknownSchemaNode> unknownSchemaNodes;
    protected List<TypeDefinition<?>> types;

    protected List<EnumPair> enumValues;
    protected List<Bit> bitValues;

    protected Integer fractionDigits;

    protected String units;
    protected Object defaultValue;

    protected String description;
    protected String reference;
    protected Status status;

    public DerivedType(StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        this.qName = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
    }

    public void initBaseType(final StmtContext<QName, TypedefStatement, ?> ctx) {
        final TypeDefinition typeStmt = firstSubstatementOfType(TypeDefinition.class);

        if (typeStmt == null) {
            throw new IllegalStateException("none type defined for node " + ctx.getStatementArgument());
        }

        final QName typeQName = typeStmt.getQName();

        if (TypeUtils.isYangBaseTypeString(typeQName.getLocalName())) {
            baseType = TypeUtils.getYangBaseTypeFromString(typeStmt.getQName().getLocalName());
        } else {
            baseType = ctx.getFromNamespace(QNameToDerivedType.class, typeStmt.getQName());
        }

        this.rangeConstraints = initRanges();
        this.lengthConstraints = initLengths();
        this.patternConstraints = initPatterns();
        this.unknownSchemaNodes = initUnknownStatements();
        this.types = initTypes();

        this.enumValues = initEnumValues();
        this.bitValues = initBitValues();

        this.fractionDigits = initFractionDigits();

        this.description = initDescription();
        this.reference = initReference();
        this.status = initStatus();
        this.units = initUnits();
        this.defaultValue = initDefaultValue();
    }

    protected List<RangeConstraint> initRanges() {
        final RangeEffectiveStatementImpl rangeConstraints = firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraints != null ? rangeConstraints.argument() : Collections.<RangeConstraint>emptyList();//baseType.getRangeConstraints();
    }

    protected List<LengthConstraint> initLengths() {
        final LengthEffectiveStatementImpl lengthConstraints = firstEffective(LengthEffectiveStatementImpl.class);
        return lengthConstraints != null ? lengthConstraints.argument() : Collections.<LengthConstraint>emptyList();//baseType.getLengthConstraints();
    }

    protected List<PatternConstraint> initPatterns() {
        final List<PatternConstraint> patternConstraints = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PatternEffectiveStatementImpl) {
                patternConstraints.add(((PatternEffectiveStatementImpl) effectiveStatement).argument());
            }
        }

        return !patternConstraints.isEmpty() ? ImmutableList.copyOf(patternConstraints) : Collections.<PatternConstraint>emptyList();//baseType
                //.getPatternConstraints();
    }

    protected List<UnknownSchemaNode> initUnknownStatements() {

        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof UnknownEffectiveStatementImpl) {
                unknownSchemaNodes.add(((UnknownEffectiveStatementImpl) effectiveStatement));
            }
        }

        return !unknownSchemaNodes.isEmpty() ? ImmutableList.copyOf(unknownSchemaNodes) : Collections.<UnknownSchemaNode>emptyList();//baseType
                //.getUnknownSchemaNodes();
    }

    private List<EnumPair> initEnumValues() {
        final List<EnumPair> enumValues = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof EnumEffectiveStatementImpl) {
                enumValues.add(((EnumEffectiveStatementImpl) effectiveStatement));
            }
        }

        return !enumValues.isEmpty() ? ImmutableList.copyOf(enumValues) : Collections.<EnumPair>emptyList();
    }


    private List<Bit> initBitValues() {
        final List<Bit> bitValues = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof BitEffectiveStatementImpl) {
                bitValues.add(((BitEffectiveStatementImpl) effectiveStatement));
            }
        }

        return !bitValues.isEmpty() ? ImmutableList.copyOf(bitValues) : Collections.<Bit>emptyList();
    }

    protected Integer initFractionDigits() {
        final FractionDigitsEffectiveStatementImpl fractionDigitsEffStmt = firstEffective(FractionDigitsEffectiveStatementImpl.class);
        return fractionDigitsEffStmt != null ? fractionDigitsEffStmt.argument() : null;//baseType.getFractionDigits();
    }

    protected String initDescription() {
        final DescriptionEffectiveStatementImpl descEffectiveStatement = firstEffective(DescriptionEffectiveStatementImpl.class);
        return descEffectiveStatement != null ? descEffectiveStatement.argument() : "";//baseType.getDescription();
    }

    protected String initReference() {
        final ReferenceEffectiveStatementImpl refEffectiveStatement = firstEffective(ReferenceEffectiveStatementImpl.class);
        return refEffectiveStatement != null ? refEffectiveStatement.argument() : "";//baseType.getReference();
    }

    protected Status initStatus() {
        final StatusEffectiveStatementImpl statusEffectiveStatement = firstEffective(StatusEffectiveStatementImpl.class);
        return statusEffectiveStatement != null ? statusEffectiveStatement.argument() : Status.CURRENT;//baseType.getStatus();
    }

    protected String initUnits() {
        final UnitsEffectiveStatementImpl unitsEffectiveStatement = firstEffective(UnitsEffectiveStatementImpl.class);
        return unitsEffectiveStatement != null ? unitsEffectiveStatement.argument() : null;//baseType.getUnits();
    }

    protected Object initDefaultValue() {
        final DefaultEffectiveStatementImpl defaultEffectiveStatement = firstEffective(DefaultEffectiveStatementImpl.class);
        return defaultEffectiveStatement != null ? defaultEffectiveStatement.argument() : null;//baseType.getDefaultValue();
    }


    private List<TypeDefinition<?>> initTypes() {
        for (final EffectiveStatement<?,?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof TypeDefinition) {
                // TODO
            }
        }
        return null;
    }

    public List<TypeDefinition<?>> getTypes() {
        return types;
    }

    public List<EnumPair> getValues() {
        return enumValues;
    }

    public List<Bit> getBits() {
        return bitValues;
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
