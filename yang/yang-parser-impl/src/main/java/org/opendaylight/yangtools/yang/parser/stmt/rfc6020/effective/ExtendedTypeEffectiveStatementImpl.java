/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.TypeDefinitionEffectiveBuilder;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

public class ExtendedTypeEffectiveStatementImpl extends EffectiveStatementBase<String, TypeStatement> implements
        TypeDefinition<TypeDefinition<?>>, TypeDefinitionEffectiveBuilder {

    private static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults();

    private final QName qName;
    private final SchemaPath path;

    private final TypeDefinition<?> baseType;

    private final String defaultValue = null;
    private final String units = null;

    private final String description = null;
    private final String reference = null;

    private final Status status = null;

    private final List<RangeConstraint> ranges;
    private final List<LengthConstraint> lengths;
    private final List<PatternConstraint> patterns;
    private final Integer fractionDigits;

    private ExtendedType extendedType = null;
    private final boolean isExtended;

    public ExtendedTypeEffectiveStatementImpl(
            StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx, boolean isExtended) {
        super(ctx);

        this.isExtended = isExtended;
        qName = initQName(ctx, isExtended);

        final StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> typeStmt = ctx
                .getFromNamespace(TypeNamespace.class, qName);
        if (typeStmt == null) {
            path = Utils.getSchemaPath(ctx);
        } else {
            path = Utils.getSchemaPath(ctx.getFromNamespace(TypeNamespace.class, qName));
        }

        ranges = initRanges();
        lengths = initLengths();
        patterns = initPatterns();
        fractionDigits = initFractionDigits();

        baseType = parseBaseTypeFromCtx(ctx);
        validateTypeConstraints(ctx);
    }

    private QName initQName(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final boolean isExtended) {

        QName qName;

        if (isExtended) {
            final Splitter colonSplitter = Splitter.on(":").trimResults();
            final List<String> nameTokens = colonSplitter.splitToList(ctx.getStatementArgument());

            switch (nameTokens.size()) {
            case 1:
                qName = QName.create(Utils.getRootModuleQName(ctx), nameTokens.get(0));
                break;
            case 2:
                qName = QName.create(Utils.getRootModuleQName(ctx), nameTokens.get(1));
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "Bad colon separated parts number (%d) of QName '%s'.", nameTokens.size(),
                        ctx.getStatementArgument()));
            }
        } else {
            qName = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
        }
        return qName;
    }

    private TypeDefinition<?> parseBaseTypeFromCtx(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

        TypeDefinition<?> baseType;

        final QName baseTypeQName = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
        if (TypeUtils.isYangPrimitiveTypeString(baseTypeQName.getLocalName())) {
            baseType = TypeUtils.getYangPrimitiveTypeFromString(baseTypeQName.getLocalName());
        } else {
            StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> baseTypeCtx = ctx
                    .getParentContext().getFromNamespace(TypeNamespace.class, baseTypeQName);

            if (baseTypeCtx == null) {
                throw new IllegalStateException(String.format("Type '%s' was not found in %s.", baseTypeQName,
                        ctx.getStatementSourceReference()));
            }

            baseType = (TypeDefEffectiveStatementImpl) baseTypeCtx.buildEffective();
        }

        return baseType;
    }

    private void validateTypeConstraints(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

        final List<String> sourceParts = COLON_SPLITTER.splitToList(ctx.getStatementSourceReference().toString());
        TypeConstraints typeConstraints = new TypeConstraints(sourceParts.get(0), Integer.parseInt(sourceParts.get(1)));

        typeConstraints.addRanges(ranges);
        typeConstraints.addLengths(lengths);
        typeConstraints.addPatterns(patterns);
        typeConstraints.addFractionDigits(fractionDigits);

        typeConstraints = addConstraintsFromBaseType(typeConstraints, baseType);
        typeConstraints.validateConstraints();
    }

    private TypeConstraints addConstraintsFromBaseType(final TypeConstraints typeConstraints,
            final TypeDefinition<?> baseType) {

        final String baseTypeName = baseType.getQName().getLocalName();

        if (baseType instanceof IntegerTypeDefinition) {
            final IntegerTypeDefinition intType = (IntegerTypeDefinition) TypeUtils
                    .getYangPrimitiveTypeFromString(baseTypeName);
            typeConstraints.addRanges(intType.getRangeConstraints());
        } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
            final UnsignedIntegerTypeDefinition uintType = (UnsignedIntegerTypeDefinition) TypeUtils
                    .getYangPrimitiveTypeFromString(baseTypeName);
            typeConstraints.addRanges(uintType.getRangeConstraints());
        } else if (baseType instanceof StringTypeDefinition) {
            final StringTypeDefinition stringType = (StringTypeDefinition) TypeUtils
                    .getYangPrimitiveTypeFromString(baseTypeName);
            typeConstraints.addLengths(stringType.getLengthConstraints());
            typeConstraints.addPatterns(stringType.getPatternConstraints());
        } else if (baseType instanceof BinaryTypeDefinition) {
            final BinaryTypeDefinition binaryType = (BinaryTypeDefinition) TypeUtils
                    .getYangPrimitiveTypeFromString(baseTypeName);
            typeConstraints.addLengths(binaryType.getLengthConstraints());
        } else if (baseType instanceof TypeDefEffectiveStatementImpl) {
            typeConstraints.addRanges(((TypeDefEffectiveStatementImpl) baseType).getRangeConstraints());
            typeConstraints.addLengths(((TypeDefEffectiveStatementImpl) baseType).getLengthConstraints());
            typeConstraints.addPatterns(((TypeDefEffectiveStatementImpl) baseType).getPatternConstraints());
            typeConstraints.addFractionDigits(((TypeDefEffectiveStatementImpl) baseType).getFractionDigits());
        }
//        else if (baseType instanceof DecimalTypeDefinition) {
//            final DecimalTypeDefinition decimalType = (DecimalTypeDefinition) TypeUtils
//                    .getYangBaseTypeFromString(baseTypeName);
//            typeConstraints.addRanges(decimalType.getRangeConstraints());
//            typeConstraints.addFractionDigits(decimalType.getFractionDigits());
//        }
//        else if (baseType instanceof ExtendedTypeEffectiveStatementImpl) {
//            typeConstraints.addRanges(((ExtendedTypeEffectiveStatementImpl) baseType).getRangeConstraints());
//            typeConstraints.addLengths(((ExtendedTypeEffectiveStatementImpl) baseType).getLengthConstraints());
//            typeConstraints.addPatterns(((ExtendedTypeEffectiveStatementImpl) baseType).getPatternConstraints());
//            typeConstraints.addFractionDigits(((ExtendedTypeEffectiveStatementImpl) baseType).getFractionDigits());
//        }

        return typeConstraints;
    }

    protected Integer initFractionDigits() {
        final FractionDigitsEffectiveStatementImpl fractionDigitsEffStmt = firstEffective(FractionDigitsEffectiveStatementImpl.class);
        return fractionDigitsEffStmt != null ? fractionDigitsEffStmt.argument() : null;
    }

    protected List<RangeConstraint> initRanges() {
        final RangeEffectiveStatementImpl rangeConstraints = firstEffective(RangeEffectiveStatementImpl.class);
        return rangeConstraints != null ? rangeConstraints.argument() : Collections.<RangeConstraint> emptyList();
    }

    protected List<LengthConstraint> initLengths() {
        final LengthEffectiveStatementImpl lengthConstraints = firstEffective(LengthEffectiveStatementImpl.class);
        return lengthConstraints != null ? lengthConstraints.argument() : Collections.<LengthConstraint> emptyList();
    }

    protected List<PatternConstraint> initPatterns() {
        final List<PatternConstraint> patternConstraints = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PatternEffectiveStatementImpl) {
                final PatternConstraint pattern = ((PatternEffectiveStatementImpl) effectiveStatement).argument();

                if (pattern != null) {
                    patternConstraints.add(pattern);
                }
            }
        }

        return !patternConstraints.isEmpty() ? ImmutableList.copyOf(patternConstraints) : Collections
                .<PatternConstraint> emptyList();
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

        if (!isExtended && baseType instanceof TypeDefEffectiveStatementImpl) {
            TypeDefEffectiveStatementImpl originalTypeDef = (TypeDefEffectiveStatementImpl) baseType;
            return originalTypeDef.buildType();
        }

        Builder extendedTypeBuilder;
        if (baseType instanceof TypeDefEffectiveStatementImpl) {
            TypeDefEffectiveStatementImpl typeDefBaseType = (TypeDefEffectiveStatementImpl) baseType;
            extendedTypeBuilder = ExtendedType.builder(qName, typeDefBaseType.buildType(),
                    Optional.fromNullable(description), Optional.fromNullable(reference), path);
        } else {
            extendedTypeBuilder = ExtendedType.builder(qName, baseType, Optional.fromNullable(description),
                    Optional.fromNullable(reference), path);
        }

        extendedTypeBuilder.fractionDigits(fractionDigits);
        extendedTypeBuilder.ranges(ranges);
        extendedTypeBuilder.lengths(lengths);
        extendedTypeBuilder.patterns(patterns);

        extendedType = extendedTypeBuilder.build();

        return extendedType;
    }
}
