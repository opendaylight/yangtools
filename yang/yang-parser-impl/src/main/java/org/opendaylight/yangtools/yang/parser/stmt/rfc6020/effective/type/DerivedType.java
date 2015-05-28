/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DefaultEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnitsEffectiveStatementImpl;

public class DerivedType extends EffectiveStatementBase<QName, TypedefStatement> implements
        TypeDefinition<TypeDefinition<?>> {

    public DerivedType(StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx) {
        super(ctx);

        qName = ctx.getStatementArgument();
        path = Utils.getSchemaPath(ctx);

        baseType = initBaseType();

        rangeConstraints = initRanges();
        lengthConstraints = initLengths();
        patternConstraints = initPatterns();

        description = initDescription();
        reference = initReference();
        status = initStatus();
        units = initUnits();
        defaultValue = initDefaultValue();
    }

    private final QName qName;
    private final SchemaPath path;
    private final TypeDefinition baseType;

    private final List<RangeConstraint> rangeConstraints;
    private final List<LengthConstraint> lengthConstraints;
    private final List<PatternConstraint> patternConstraints;

    private final String units;
    private final Object defaultValue;

    private final String description;
    private final String reference;
    private final Status status;

    private TypeDefinition initBaseType() {
        return firstSubstatementOfType(TypeDefinition.class);
    }

    private List<RangeConstraint> initRanges() {
        final RangeEffectiveStatementImpl rangeConstraints = firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraints != null ? rangeConstraints.argument() : Collections.<RangeConstraint> emptyList();
    }

    private List<LengthConstraint> initLengths() {
        return null;
    }

    private List<PatternConstraint> initPatterns() {
        List<PatternConstraint> patternConstraints = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PatternEffectiveStatementImpl) {
                patternConstraints.add(((PatternEffectiveStatementImpl) effectiveStatement).argument());
            }
        }

        return ImmutableList.copyOf(patternConstraints);
    }

    private String initDescription() {
        final DescriptionEffectiveStatementImpl descEffectiveStatement = firstEffective(DescriptionEffectiveStatementImpl.class);
        return descEffectiveStatement != null ? descEffectiveStatement.argument() : "";
    }

    private String initReference() {
        final ReferenceEffectiveStatementImpl refEffectiveStatement = firstEffective(ReferenceEffectiveStatementImpl.class);
        return refEffectiveStatement != null ? refEffectiveStatement.argument() : "";
    }

    private Status initStatus() {
        final StatusEffectiveStatementImpl statusEffectiveStatement = firstEffective(StatusEffectiveStatementImpl.class);
        return statusEffectiveStatement != null ? statusEffectiveStatement.argument() : Status.CURRENT;
    }

    private String initUnits() {
        final UnitsEffectiveStatementImpl unitsEffectiveStatement = firstEffective(UnitsEffectiveStatementImpl.class);
        return unitsEffectiveStatement != null ? unitsEffectiveStatement.argument() : "";
    }

    private Object initDefaultValue() {
        final DefaultEffectiveStatementImpl defaultEffectiveStatement = firstEffective(DefaultEffectiveStatementImpl.class);
        return defaultEffectiveStatement != null ? defaultEffectiveStatement.argument() : null;
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
        return Collections.emptyList();
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
