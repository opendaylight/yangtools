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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeEffectiveStatementImpl;

public class ExtendedTypeEffectiveStatementImpl<T extends TypeDefinition<?>, D extends TypeStatement> extends
        EffectiveStatementBase<String, D> implements TypeDefinition<T> {

    private final QName qName;
    private final SchemaPath path;

    private T baseType;

    private String defaultValue;
    private String units;

    private String description;
    private String reference;

    private Status status;

    private List<RangeConstraint> ranges = Collections.emptyList();
    private List<LengthConstraint> lengths = Collections.emptyList();
    private List<PatternConstraint> patterns = Collections.emptyList();
    private Integer fractionDigits = null;

    private ExtendedType extendedType = null;

    public ExtendedTypeEffectiveStatementImpl(StmtContext<String, D, EffectiveStatement<String, D>> ctx) {
        super(ctx);

        if (!effectiveSubstatements().isEmpty()) {

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

        final StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> typeStmt = ctx
                .getFromNamespace(TypeNamespace.class, qName);
        if (typeStmt == null) {
            path = Utils.getSchemaPath(ctx);
        } else {
            path = Utils.getSchemaPath(ctx.getFromNamespace(TypeNamespace.class, qName));
        }

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
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

        ranges = initRanges();
        lengths = initLengths();
        patterns = initPatterns();
        fractionDigits = initFractionDigits();

        final QName baseTypeQName = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
        if (TypeUtils.isYangBaseTypeString(baseTypeQName.getLocalName())) {
            baseType = (T) TypeUtils.getYangBaseTypeFromString(baseTypeQName.getLocalName(), this);
        } else {
            StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> baseTypeCtx = ctx
                    .getParentContext().getFromNamespace(TypeNamespace.class, baseTypeQName);
            baseType = (T) baseTypeCtx.buildEffective();
        }

        // if(baseType == null) baseType = Decimal64BaseType.getInstance();
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
                patternConstraints.add(((PatternEffectiveStatementImpl) effectiveStatement).argument());
            }
        }

        return !patternConstraints.isEmpty() ? ImmutableList.copyOf(patternConstraints) : Collections
                .<PatternConstraint> emptyList();
    }

    @Override
    public T getBaseType() {
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

    public ExtendedType buildExtendedType() {

        if (extendedType != null) {
            return extendedType;
        }

        Builder extendedTypeBuilder = null;
        if (baseType instanceof TypeDefEffectiveStatementImpl) {
            TypeDefEffectiveStatementImpl typeDefBaseType = (TypeDefEffectiveStatementImpl) baseType;
            extendedTypeBuilder = ExtendedType.builder(qName, typeDefBaseType.buildExtendedType(),
                    Optional.fromNullable(description), Optional.fromNullable(reference), path);
        } else {
            extendedTypeBuilder = ExtendedType.builder(qName, baseType, Optional.fromNullable(description),
                    Optional.fromNullable(reference), path);
        }

        extendedTypeBuilder.fractionDigits(fractionDigits);
        extendedTypeBuilder.ranges(ranges);
        extendedTypeBuilder.lengths(lengths);
        extendedTypeBuilder.patterns(patterns);

        return extendedTypeBuilder.build();
    }
}
