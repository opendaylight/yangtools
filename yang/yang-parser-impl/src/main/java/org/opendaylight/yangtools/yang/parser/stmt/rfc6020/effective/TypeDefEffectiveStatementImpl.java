/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Verify;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public final class TypeDefEffectiveStatementImpl extends AbstractEffectiveSchemaNode<TypedefStatement> implements
        TypeDefinition<TypeDefinition<?>>, TypeDefinitionEffectiveBuilder {
    private final TypeDefinition<?> baseType;
    private final String defaultValue;
    private final String units;
    private final List<RangeConstraint> ranges;
    private final List<LengthConstraint> lengths;
    private final List<PatternConstraint> patterns;
    private final Integer fractionDigits;
    private ExtendedType extendedType = null;

    public TypeDefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);
        baseType = parseBaseTypeFromCtx(ctx);

        UnitsEffectiveStatementImpl unitsStmt = firstEffective(UnitsEffectiveStatementImpl.class);
        this.units = (unitsStmt == null) ? null : unitsStmt.argument();
        DefaultEffectiveStatementImpl defaultStmt = firstEffective(DefaultEffectiveStatementImpl.class);
        this.defaultValue = (defaultStmt == null) ? null : defaultStmt.argument();

        EffectiveStatementBase<?, ?> typeEffectiveStmt = firstSubstatementOfType(TypeDefinition.class,
                EffectiveStatementBase.class);
        ranges = initRanges(typeEffectiveStmt);
        lengths = initLengths(typeEffectiveStmt);
        patterns = initPatterns(typeEffectiveStmt);

        // due to compatibility problems with original yang parser
        // :FIXME try to find out better solution
        if (typeEffectiveStmt.argument().equals(TypeUtils.DECIMAL64) && ranges.isEmpty()) {
            fractionDigits = null;
        } else {
            fractionDigits = initFractionDigits(typeEffectiveStmt);
        }
    }

    private TypeDefinition<?> parseBaseTypeFromCtx(final StmtContext<QName, TypedefStatement, ?> ctx) {

        TypeDefinition<?> baseTypeInit;

        QName baseTypeQName = Utils.qNameFromArgument(ctx,
                StmtContextUtils.firstAttributeOf(ctx.declaredSubstatements(), TypeStatement.class));

        if (TypeUtils.isYangBuiltInTypeString(baseTypeQName.getLocalName())) {
            baseTypeInit = TypeUtils.getYangPrimitiveTypeFromString(baseTypeQName.getLocalName());
            if (baseTypeInit == null) {
                baseTypeInit = firstSubstatementOfType(TypeDefinition.class);

                // due to compatibility problems with original yang parser
                // :FIXME try to find out better solution
                if (baseTypeInit instanceof Decimal64SpecificationEffectiveStatementImpl) {
                    Decimal64SpecificationEffectiveStatementImpl decimal64 = (Decimal64SpecificationEffectiveStatementImpl) baseTypeInit;
                    if (decimal64.isExtended()) {
                        baseTypeInit = decimal64.getBaseType();
                    }
                }
            }
        } else {
            StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> baseTypeCtx = ctx
                    .getParentContext().getFromNamespace(TypeNamespace.class, baseTypeQName);
            baseTypeInit = (TypeDefEffectiveStatementImpl) baseTypeCtx.buildEffective();
        }

        return baseTypeInit;
    }

    protected Integer initFractionDigits(final EffectiveStatementBase<?, ?> typeEffectiveStmt) {
        final FractionDigitsEffectiveStatementImpl fractionDigitsEffStmt = typeEffectiveStmt
                .firstEffective(FractionDigitsEffectiveStatementImpl.class);
        return fractionDigitsEffStmt != null ? fractionDigitsEffStmt.argument() : null;
    }

    protected List<RangeConstraint> initRanges(final EffectiveStatementBase<?, ?> typeEffectiveStmt) {
        final RangeEffectiveStatementImpl rangeConstraints = typeEffectiveStmt
                .firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraints != null ? rangeConstraints.argument() : Collections.<RangeConstraint> emptyList();
    }

    protected List<LengthConstraint> initLengths(final EffectiveStatementBase<?, ?> typeEffectiveStmt) {
        final LengthEffectiveStatementImpl lengthConstraints = typeEffectiveStmt
                .firstEffective(LengthEffectiveStatementImpl.class);
        return lengthConstraints != null ? lengthConstraints.argument() : Collections.<LengthConstraint> emptyList();
    }

    protected List<PatternConstraint> initPatterns(final EffectiveStatementBase<?, ?> typeEffectiveStmt) {
        final List<PatternConstraint> patternConstraints = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : typeEffectiveStmt.effectiveSubstatements()) {
            if (effectiveStatement instanceof PatternEffectiveStatementImpl) {
                final PatternConstraint pattern = ((PatternEffectiveStatementImpl) effectiveStatement).argument();
                if (pattern != null) {
                    patternConstraints.add(pattern);
                }
            }
        }

        return ImmutableList.copyOf(patternConstraints);
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
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
            extendedTypeBuilder = ExtendedType.builder(getQName(), typeDefBaseType.buildType(),
                    Optional.fromNullable(getDescription()), Optional.fromNullable(getReference()), getPath());
        } else {
            extendedTypeBuilder = ExtendedType.builder(getQName(), baseType, Optional.fromNullable(getDescription()),
                    Optional.fromNullable(getReference()), getPath());
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
