/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidRangeConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractTypeStatementSupport extends AbstractTypeSupport<TypeStatement>
        implements UndeclaredStatementFactory<QName, TypeStatement, TypeEffectiveStatement<TypeStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.TYPE)
            .addOptional(BaseStatement.DEFINITION)
            .addAny(BitStatement.DEFINITION)
            .addAny(EnumStatement.DEFINITION)
            .addOptional(FractionDigitsStatement.DEFINITION)
            .addOptional(LengthStatement.DEFINITION)
            .addOptional(YangStmtMapping.PATH)
            .addAny(PatternStatement.DEFINITION)
            .addOptional(RangeStatement.DEFINITION)
            .addOptional(RequireInstanceStatement.DEFINITION)
            .addAny(YangStmtMapping.TYPE)
            .build();

    private static final ImmutableMap<QName, BuiltinEffectiveStatement> STATIC_BUILT_IN_TYPES =
        ImmutableMap.<QName, BuiltinEffectiveStatement>builder()
            .put(TypeDefinitions.BINARY, BuiltinEffectiveStatement.BINARY)
            .put(TypeDefinitions.BOOLEAN, BuiltinEffectiveStatement.BOOLEAN)
            .put(TypeDefinitions.EMPTY, BuiltinEffectiveStatement.EMPTY)
            // FIXME: this overlaps with DYNAMIC_BUILT_IN_TYPES. One of these is not needed, but we need to decide
            //        what to do. I think we should gradually use per-statement validators, hence go towards dynamic?
            .put(TypeDefinitions.INSTANCE_IDENTIFIER, BuiltinEffectiveStatement.INSTANCE_IDENTIFIER)
            .put(TypeDefinitions.INT8, BuiltinEffectiveStatement.INT8)
            .put(TypeDefinitions.INT16, BuiltinEffectiveStatement.INT16)
            .put(TypeDefinitions.INT32, BuiltinEffectiveStatement.INT32)
            .put(TypeDefinitions.INT64, BuiltinEffectiveStatement.INT64)
            .put(TypeDefinitions.STRING, BuiltinEffectiveStatement.STRING)
            .put(TypeDefinitions.UINT8, BuiltinEffectiveStatement.UINT8)
            .put(TypeDefinitions.UINT16, BuiltinEffectiveStatement.UINT16)
            .put(TypeDefinitions.UINT32, BuiltinEffectiveStatement.UINT32)
            .put(TypeDefinitions.UINT64, BuiltinEffectiveStatement.UINT64)
            .build();

    private final ImmutableMap<String, StatementSupport<?, ?, ?>> dynamicBuiltInTypes;

    AbstractTypeStatementSupport(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
        dynamicBuiltInTypes = ImmutableMap.<String, StatementSupport<?, ?, ?>>builder()
            .put(TypeDefinitions.BITS.getLocalName(), new BitsSpecificationSupport(config))
            .put(TypeDefinitions.DECIMAL64.getLocalName(), new Decimal64SpecificationSupport(config))
            .put(TypeDefinitions.ENUMERATION.getLocalName(), new EnumSpecificationSupport(config))
            .put(TypeDefinitions.IDENTITYREF.getLocalName(), IdentityRefSpecificationSupport.rfc6020Instance(config))
            .put(TypeDefinitions.INSTANCE_IDENTIFIER.getLocalName(), new InstanceIdentifierSpecificationSupport(config))
            .put(TypeDefinitions.LEAFREF.getLocalName(), LeafrefSpecificationSupport.rfc6020Instance(config))
            .put(TypeDefinitions.UNION.getLocalName(), new UnionSpecificationSupport(config))
            .build();
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<QName, TypeStatement, EffectiveStatement<QName, TypeStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final var typeQName = stmt.getArgument();
        final var builtin = STATIC_BUILT_IN_TYPES.get(typeQName);
        if (builtin != null) {
            stmt.addToNs(BaseTypeNamespace.INSTANCE, Empty.value(), builtin);
            return;
        }

        final var typeAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var typePrereq = typeAction.requiresCtx(stmt, ParserNamespaces.TYPE, typeQName,
            ModelProcessingPhase.EFFECTIVE_MODEL);
        typeAction.mutatesEffectiveCtx(stmt.getParentContext());

        /*
         * If the type does not exist, throw an InferenceException.
         * If the type exists, store a reference to it in BaseTypeNamespace.
         */
        typeAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                // Note: do not attempt to call buildEffective() here
                stmt.addToNs(BaseTypeNamespace.INSTANCE, Empty.value(), typePrereq.resolve(ctx));
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(typePrereq), stmt, "Type [%s] was not found.", typeQName);
            }
        });
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return !dynamicBuiltInTypes.isEmpty();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        return dynamicBuiltInTypes.get(argument);
    }

    @Override
    public final TypeEffectiveStatement<TypeStatement> createUndeclaredEffective(
            final UndeclaredCurrent<QName, TypeStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        final var substatements = buildEffectiveSubstatements(stmt,
            statementsToBuild(stmt, effectiveSubstatements.filter(StmtContext::isSupportedToBuildEffective)));

        // First look up the proper base type
        final var typeStmt = resolveType(stmt);
        if (substatements.isEmpty()) {
            return typeStmt;
        }

        // TODO: mirror the logic below
        throw new UnsupportedOperationException("Non-empty undeclared type statements are not implemented yet");
    }

    @Override
    protected final TypeStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            final var builtin = BuiltinTypeStatement.lookup(ctx.getRawArgument());
            if (builtin != null) {
                return builtin;
            }
        }
        return DeclaredStatements.createType(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final TypeStatement attachDeclarationReference(final TypeStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateType(stmt, reference);
    }

    @Override
    protected EffectiveStatement<QName, TypeStatement> createEffective(final Current<QName, TypeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // First look up the proper base type
        final var typeStmt = resolveType(stmt);
        if (substatements.isEmpty()) {
            return typeStmt;
        }

        // Now instantiate the proper effective statement for that type
        final var baseType = typeStmt.getTypeDefinition();
        final var declared = stmt.declared();
        return switch (baseType) {
            case BinaryTypeDefinition def -> createBinary(stmt, def, declared, substatements);
            case BitsTypeDefinition def -> createBits(stmt, def, declared, substatements);
            case BooleanTypeDefinition def -> createBoolean(stmt, def, declared, substatements);
            case DecimalTypeDefinition def -> createDecimal(stmt, def, declared, substatements);
            case EmptyTypeDefinition def ->  createEmpty(stmt, def, declared, substatements);
            case EnumTypeDefinition def -> createEnum(stmt, def, declared, substatements);
            case IdentityrefTypeDefinition def -> createIdentityref(stmt, def, declared, substatements);
            case InstanceIdentifierTypeDefinition def -> createInstanceIdentifier(stmt, def, declared, substatements);
            case Int8TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt8Builder(def, typeEffectiveQName(stmt)));
            case Int16TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt16Builder(def, typeEffectiveQName(stmt)));
            case Int32TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt32Builder(def, typeEffectiveQName(stmt)));
            case Int64TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt64Builder(def, typeEffectiveQName(stmt)));
            case LeafrefTypeDefinition def -> createLeafref(stmt, def, declared, substatements);
            case StringTypeDefinition def -> createString(stmt, def, declared, substatements);
            case Uint8TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint8Builder(def, typeEffectiveQName(stmt)));
            case Uint16TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint16Builder(def, typeEffectiveQName(stmt)));
            case Uint32TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint32Builder(def, typeEffectiveQName(stmt)));
            case Uint64TypeDefinition def -> createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint64Builder(def, typeEffectiveQName(stmt)));
            case UnionTypeDefinition def -> createUnion(stmt, def, declared, substatements);
            default -> throw new IllegalStateException("Unhandled base type " + baseType);
        };
    }

    // FIXME: YANGTOOLS-1208: this needs to happen during onFullDefinitionDeclared() and stored (again) in a namespace
    static final @NonNull QName typeEffectiveQName(final Current<QName, ?> stmt) {
        return stmt.getArgument().bindTo(stmt.getEffectiveParent().effectiveNamespace()).intern();
    }

    /**
     * Resolve type reference, as pointed to by the context's argument.
     *
     * @param ctx Statement context
     * @return Resolved type
     * @throws SourceException if the target type cannot be found
     */
    private static @NonNull TypeEffectiveStatement<TypeStatement> resolveType(final NamespaceStmtCtx ctx) {
        final var obj = verifyNotNull(ctx.namespaceItem(BaseTypeNamespace.INSTANCE, Empty.value()));
        return switch (obj) {
            case BuiltinEffectiveStatement builtin -> builtin;
            case StmtContext<?, ?, ?> stmt ->
                ((TypedefEffectiveStatement) stmt.buildEffective()).asTypeEffectiveStatement();
            default -> throw new InferenceException(ctx, "Unexpected base object %s", obj);
        };
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBinary(final Current<QName, ?> ctx,
            final BinaryTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newBinaryBuilder(baseType, typeEffectiveQName(ctx));

        for (var stmt : substatements) {
            if (stmt instanceof LengthEffectiveStatement length) {
                try {
                    builder.setLengthConstraint(length, length.argument());
                } catch (IllegalStateException e) {
                    throw new SourceException(ctx, e, "Multiple length constraints encountered");
                } catch (InvalidLengthConstraintException e) {
                    throw new SourceException(ctx, e, "Invalid length constraint %s", length.argument());
                }
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private @NonNull TypeEffectiveStatement<TypeStatement> createBits(final Current<?, ?> ctx,
            final BitsTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newBitsBuilder(baseType, ctx.argumentAsTypeQName());

        for (var stmt : substatements) {
            if (stmt instanceof BitEffectiveStatement bit) {
                builder.addBit(addRestrictedBit(ctx, baseType, bit));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    abstract @NonNull Bit addRestrictedBit(@NonNull EffectiveStmtCtx stmt, @NonNull BitsTypeDefinition base,
        @NonNull BitEffectiveStatement bit);

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBoolean(final Current<QName, ?> ctx,
            final BooleanTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newBooleanBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createDecimal(final Current<QName, ?> ctx,
            final DecimalTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newDecima64Builder(baseType, typeEffectiveQName(ctx));

        for (var stmt : substatements) {
            switch (stmt) {
                case RangeEffectiveStatement range ->  builder.setRangeConstraint(range, range.argument());
                case FractionDigitsEffectiveStatement fdes -> {
                    final var digits = fdes.argument();
                    SourceException.throwIf(baseType.getFractionDigits() != digits, ctx,
                        "Cannot override fraction-digits from base type %s to %s", baseType, digits);
                }
                default -> {
                    // no-op
                }
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createEmpty(final Current<QName, ?> ctx,
            final EmptyTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newEmptyBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private @NonNull TypeEffectiveStatement<TypeStatement> createEnum(final Current<?, ?> ctx,
            final EnumTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newEnumerationBuilder(baseType, ctx.argumentAsTypeQName());

        for (var stmt : substatements) {
            if (stmt instanceof EnumEffectiveStatement ees) {
                builder.addEnum(addRestrictedEnum(ctx, baseType, ees));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    abstract @NonNull EnumPair addRestrictedEnum(@NonNull EffectiveStmtCtx stmt, @NonNull EnumTypeDefinition base,
        @NonNull EnumEffectiveStatement enumStmt);

    private static @NonNull TypeEffectiveStatement<TypeStatement> createIdentityref(final Current<QName, ?> ctx,
            final IdentityrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newIdentityrefBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createInstanceIdentifier(final Current<QName, ?> ctx,
            final InstanceIdentifierTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newInstanceIdentifierBuilder(baseType, typeEffectiveQName(ctx));

        for (var stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement ries) {
                builder.setRequireInstance(ries.argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static <T extends RangeRestrictedTypeDefinition<T, N>, N extends Number & Comparable<N>>
        @NonNull TypeEffectiveStatement<TypeStatement> createIntegral(final Current<?, ?> ctx,
                final TypeStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final RangeRestrictedTypeBuilder<T, N> builder) {
        for (var stmt : substatements) {
            if (stmt instanceof RangeEffectiveStatement range) {
                builder.setRangeConstraint(range, range.argument());
            }
        }

        try {
            return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
        } catch (InvalidRangeConstraintException e) {
            throw new SourceException(ctx, e, "Invalid range constraint: %s", e.getOffendingRanges());
        }
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createLeafref(final Current<QName, ?> ctx,
            final LeafrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newLeafrefBuilder(baseType,
            AbstractTypeStatementSupport.typeEffectiveQName(ctx));

        for (var stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement ries) {
                builder.setRequireInstance(ries.argument());
            }
        }
        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createString(final Current<QName, ?> ctx,
            final StringTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var builder = RestrictedTypes.newStringBuilder(baseType,
            AbstractTypeStatementSupport.typeEffectiveQName(ctx));

        for (var stmt : substatements) {
            switch (stmt) {
                case LengthEffectiveStatement length -> {
                    try {
                        builder.setLengthConstraint(length, length.argument());
                    } catch (IllegalStateException e) {
                        throw new SourceException(ctx, e, "Multiple length constraints encountered");
                    } catch (InvalidLengthConstraintException e) {
                        throw new SourceException(ctx, e, "Invalid length constraint %s", length.argument());
                    }
                }
                case PatternEffectiveStatement pattern -> builder.addPatternConstraint(pattern);
                default -> {
                    // No-op
                }
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createUnion(final Current<QName, ?> ctx,
            final UnionTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newUnionBuilder(baseType,
            typeEffectiveQName(ctx)));
    }
}
