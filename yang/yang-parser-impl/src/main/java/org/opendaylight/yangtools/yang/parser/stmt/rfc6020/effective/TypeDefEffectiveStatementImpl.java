/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.TypeDefinitionEffectiveBuilder;

public class TypeDefEffectiveStatementImpl extends EffectiveStatementBase<QName, TypedefStatement> implements
        TypeDefinition<TypeDefinition<?>>, TypeDefinitionEffectiveBuilder {

    private final QName qName;
    private final SchemaPath path;

    private final TypeDefinition<?> baseType;

    private String defaultValue;
    private String units;

    private String description;
    private String reference;

    private Status status;

    private final List<RangeConstraint> ranges;
    private final List<LengthConstraint> lengths;
    private final List<PatternConstraint> patterns;
    private final Integer fractionDigits;

    private ExtendedType extendedType = null;

    public TypeDefEffectiveStatementImpl(StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        qName = ctx.getStatementArgument();
        path = Utils.getSchemaPath(ctx);

        ExtendedTypeEffectiveStatementImpl type = null;

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ExtendedTypeEffectiveStatementImpl) {
                type = ((ExtendedTypeEffectiveStatementImpl) effectiveStatement);
            }
            if (effectiveStatement instanceof DefaultEffectiveStatementImpl) {
                defaultValue = ((DefaultEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof UnitsEffectiveStatementImpl) {
                units = ((UnitsEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) effectiveStatement).argument();
            }
        }

        if (type != null) {

            ranges = ImmutableList.copyOf(type.getRangeConstraints());
            lengths = ImmutableList.copyOf(type.getLengthConstraints());
            patterns = ImmutableList.copyOf(type.getPatternConstraints());
            fractionDigits = type.getFractionDigits();
        } else {

            ranges = Collections.emptyList();
            lengths = Collections.emptyList();
            patterns = Collections.emptyList();
            fractionDigits = null;
        }

        baseType = parseBaseTypeFromCtx(ctx);
    }

    private TypeDefinition<?> parseBaseTypeFromCtx(final StmtContext<QName, TypedefStatement, ?> ctx) {

        TypeDefinition<?> baseType;

        QName baseTypeQName = Utils.qNameFromArgument(ctx,
                StmtContextUtils.firstAttributeOf(ctx.declaredSubstatements(), TypeStatement.class));

        if (TypeUtils.isYangBaseTypeString(baseTypeQName.getLocalName())) {
            baseType = TypeUtils.getYangBaseTypeFromString(baseTypeQName.getLocalName());
        } else {
            StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> baseTypeCtx = ctx
                    .getParentContext().getFromNamespace(TypeNamespace.class, baseTypeQName);
            baseType = (TypeDefEffectiveStatementImpl) baseTypeCtx.buildEffective();
        }

        if (baseType == null) {
            baseType = firstSubstatementOfType(TypeDefinition.class);
        }

        return baseType;
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

    public List<RangeConstraint> getRangeConstraints() {
        return ranges;
    }

    public List<LengthConstraint> getLengthConstraints() {
        return lengths;
    }

    public List<PatternConstraint> getPatternConstraints() {
        return patterns;
    }

    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public ExtendedType buildType() {

        if (extendedType != null) {
            return extendedType;
        }

        Builder extendedTypeBuilder;
        if (baseType instanceof TypeDefinitionEffectiveBuilder) {
            TypeDefinitionEffectiveBuilder typeDefBaseType = (TypeDefinitionEffectiveBuilder) baseType;
            extendedTypeBuilder = ExtendedType.builder(qName, typeDefBaseType.buildType(),
                    Optional.fromNullable(description), Optional.fromNullable(reference), path);
        } else {
            extendedTypeBuilder = ExtendedType.builder(qName, baseType, Optional.fromNullable(description),
                    Optional.fromNullable(reference), path);
        }

        extendedTypeBuilder.defaultValue(defaultValue);
        extendedTypeBuilder.units(units);

        extendedTypeBuilder.fractionDigits(fractionDigits);
        extendedTypeBuilder.ranges(ranges);
        extendedTypeBuilder.lengths(lengths);
        extendedTypeBuilder.patterns(patterns);

        extendedType = extendedTypeBuilder.build();

        return extendedType;
    }
}
