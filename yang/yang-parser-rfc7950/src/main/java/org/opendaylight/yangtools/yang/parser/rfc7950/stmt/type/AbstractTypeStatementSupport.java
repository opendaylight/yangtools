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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
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
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.InstanceIdentifierTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidRangeConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.LengthRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.RequireInstanceRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.ri.type.StringTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractTypeStatementSupport
        extends AbstractStringStatementSupport<TypeStatement, EffectiveStatement<String, TypeStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.BASE)
        .addAny(YangStmtMapping.BIT)
        .addAny(YangStmtMapping.ENUM)
        .addOptional(YangStmtMapping.FRACTION_DIGITS)
        .addOptional(YangStmtMapping.LENGTH)
        .addOptional(YangStmtMapping.PATH)
        .addAny(YangStmtMapping.PATTERN)
        .addOptional(YangStmtMapping.RANGE)
        .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
        .addAny(YangStmtMapping.TYPE)
        .build();

    static final String BINARY = "binary";
    static final String BITS = "bits";
    static final String BOOLEAN = "boolean";
    static final String DECIMAL64 = "decimal64";
    static final String EMPTY = "empty";
    static final String ENUMERATION = "enumeration";
    static final String IDENTITY_REF = "identityref";
    static final String INSTANCE_IDENTIFIER = "instance-identifier";
    static final String INT8 = "int8";
    static final String INT16 = "int16";
    static final String INT32 = "int32";
    static final String INT64 = "int64";
    static final String LEAF_REF = "leafref";
    static final String STRING = "string";
    static final String UINT8 = "uint8";
    static final String UINT16 = "uint16";
    static final String UINT32 = "uint32";
    static final String UINT64 = "uint64";
    static final String UNION = "union";

    private static final ImmutableMap<String, BuiltinEffectiveStatement> STATIC_BUILT_IN_TYPES =
        ImmutableMap.<String, BuiltinEffectiveStatement>builder()
            .put(BINARY, BuiltinEffectiveStatement.BINARY)
            .put(BOOLEAN, BuiltinEffectiveStatement.BOOLEAN)
            .put(EMPTY, BuiltinEffectiveStatement.EMPTY)
            // FIXME: this overlaps with DYNAMIC_BUILT_IN_TYPES. One of these is not needed, but we need to decide
            //        what to do. I think we should gradually use per-statement validators, hence go towards dynamic?
            .put(INSTANCE_IDENTIFIER, BuiltinEffectiveStatement.INSTANCE_IDENTIFIER)
            .put(INT8, BuiltinEffectiveStatement.INT8)
            .put(INT16, BuiltinEffectiveStatement.INT16)
            .put(INT32, BuiltinEffectiveStatement.INT32)
            .put(INT64, BuiltinEffectiveStatement.INT64)
            .put(STRING, BuiltinEffectiveStatement.STRING)
            .put(UINT8, BuiltinEffectiveStatement.UINT8)
            .put(UINT16, BuiltinEffectiveStatement.UINT16)
            .put(UINT32, BuiltinEffectiveStatement.UINT32)
            .put(UINT64, BuiltinEffectiveStatement.UINT64)
            .build();

    private static final ImmutableMap<String, StatementSupport<?, ?, ?>> DYNAMIC_BUILT_IN_TYPES =
            ImmutableMap.<String, StatementSupport<?, ?, ?>>builder()
            .put(BITS, new BitsSpecificationSupport())
            .put(DECIMAL64, new Decimal64SpecificationSupport())
            .put(ENUMERATION, new EnumSpecificationSupport())
            .put(IDENTITY_REF, new IdentityRefSpecificationRFC6020Support())
            .put(INSTANCE_IDENTIFIER, new InstanceIdentifierSpecificationSupport())
            .put(LEAF_REF, new LeafrefSpecificationRFC6020Support())
            .put(UNION, new UnionSpecificationSupport())
            .build();

    private static final ImmutableMap<String, String> BUILT_IN_TYPES = Maps.uniqueIndex(ImmutableSet.copyOf(
        Iterables.<String>concat(STATIC_BUILT_IN_TYPES.keySet(), DYNAMIC_BUILT_IN_TYPES.keySet())), key -> key);

    AbstractTypeStatementSupport() {
        super(YangStmtMapping.TYPE, StatementPolicy.exactReplica());
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<String, TypeStatement, EffectiveStatement<String, TypeStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final String argument = stmt.getArgument();
        final BuiltinEffectiveStatement builtin = STATIC_BUILT_IN_TYPES.get(argument);
        if (builtin != null) {
            stmt.addToNs(BaseTypeNamespace.class, Empty.getInstance(), builtin);
            return;
        }

        final QName typeQName = StmtContextUtils.parseNodeIdentifier(stmt, argument);
        final ModelActionBuilder typeAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<StmtContext<?, ?, ?>> typePrereq = typeAction.requiresCtx(stmt, TypeNamespace.class,
                typeQName, ModelProcessingPhase.EFFECTIVE_MODEL);
        typeAction.mutatesEffectiveCtx(stmt.getParentContext());

        /*
         * If the type does not exist, throw an InferenceException.
         * If the type exists, store a reference to it in BaseTypeNamespace.
         */
        typeAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                // Note: do not attempt to call buildEffective() here
                stmt.addToNs(BaseTypeNamespace.class, Empty.getInstance(), typePrereq.resolve(ctx));
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(typePrereq), stmt, "Type [%s] was not found.", typeQName);
            }
        });
    }

    @Override
    public final String internArgument(final String rawArgument) {
        final String found;
        return (found = BUILT_IN_TYPES.get(rawArgument)) != null ? found : rawArgument;
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return !DYNAMIC_BUILT_IN_TYPES.isEmpty();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        return DYNAMIC_BUILT_IN_TYPES.get(argument);
    }

    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected final TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createType(ctx.getRawArgument(), substatements);
    }

    @Override
    protected final TypeStatement createEmptyDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
        final TypeStatement builtin;
        return (builtin = BuiltinTypeStatement.lookup(ctx)) != null ? builtin
            : DeclaredStatements.createType(ctx.getRawArgument());
    }

    @Override
    protected EffectiveStatement<String, TypeStatement> createEffective(final Current<String, TypeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // First look up the proper base type
        final TypeEffectiveStatement<TypeStatement> typeStmt = resolveType(stmt);
        if (substatements.isEmpty()) {
            return typeStmt;
        }

        // Now instantiate the proper effective statement for that type
        final TypeDefinition<?> baseType = typeStmt.getTypeDefinition();
        final TypeStatement declared = stmt.declared();
        if (baseType instanceof BinaryTypeDefinition) {
            return createBinary(stmt, (BinaryTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof BitsTypeDefinition) {
            return createBits(stmt, (BitsTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return createBoolean(stmt, (BooleanTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return createDecimal(stmt, (DecimalTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return createEmpty(stmt, (EmptyTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof EnumTypeDefinition) {
            return createEnum(stmt, (EnumTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return createIdentityref(stmt, (IdentityrefTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return createInstanceIdentifier(stmt, (InstanceIdentifierTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof Int8TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt8Builder((Int8TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Int16TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt16Builder((Int16TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Int32TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt32Builder((Int32TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Int64TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newInt64Builder((Int64TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return createLeafref(stmt, (LeafrefTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof StringTypeDefinition) {
            return createString(stmt, (StringTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint8Builder((Uint8TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Uint16TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint16Builder((Uint16TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Uint32TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint32Builder((Uint32TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof Uint64TypeDefinition) {
            return createIntegral(stmt, declared, substatements,
                RestrictedTypes.newUint64Builder((Uint64TypeDefinition) baseType, typeEffectiveQName(stmt)));
        } else if (baseType instanceof UnionTypeDefinition) {
            return createUnion(stmt, (UnionTypeDefinition) baseType, declared, substatements);
        } else {
            throw new IllegalStateException("Unhandled base type " + baseType);
        }
    }

    // FIXME: YANGTOOLS-1208: this needs to happen during onFullDefinitionDeclared() and stored (again) in a namespace
    static final @NonNull QName typeEffectiveQName(final Current<String, ?> stmt) {
        // FIXME: YANGTOOLS-1117: this really should be handled through A=AbstractQName, with two values coming out of
        //        parseArgument(): either UnqualifiedQName or QualifiedQName. Each of those can easily be bound to
        //        parent module:
        //          stmt.getArgument().bindTo(parentNamespace).
        final String argument = stmt.getArgument();
        return QName.create(stmt.getEffectiveParent().effectiveNamespace(),
            // Split out localName event if it is prefixed. This should really be in parseArgument()
            argument.substring(argument.indexOf(':') + 1)).intern();
    }

    /**
     * Resolve type reference, as pointed to by the context's argument.
     *
     * @param ctx Statement context
     * @return Resolved type
     * @throws SourceException if the target type cannot be found
     */
    private static @NonNull TypeEffectiveStatement<TypeStatement> resolveType(final Current<String, ?> ctx) {
        final Object obj = verifyNotNull(ctx.namespaceItem(BaseTypeNamespace.class, Empty.getInstance()));
        if (obj instanceof BuiltinEffectiveStatement) {
            return (BuiltinEffectiveStatement) obj;
        } else if (obj instanceof StmtContext) {
            return ((TypedefEffectiveStatement) ((StmtContext<?, ?, ?>) obj).buildEffective())
                .asTypeEffectiveStatement();
        } else {
            throw new InferenceException(ctx, "Unexpected base object %s", obj);
        }
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBinary(final Current<String, ?> ctx,
            final BinaryTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final LengthRestrictedTypeBuilder<BinaryTypeDefinition> builder =
                RestrictedTypes.newBinaryBuilder(baseType, typeEffectiveQName(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof LengthEffectiveStatement) {
                final LengthEffectiveStatement length = (LengthEffectiveStatement)stmt;

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
        final BitsTypeBuilder builder = RestrictedTypes.newBitsBuilder(baseType, ctx.argumentAsTypeQName());

        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof BitEffectiveStatement) {
                builder.addBit(addRestrictedBit(ctx, baseType, (BitEffectiveStatement) stmt));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    abstract @NonNull Bit addRestrictedBit(@NonNull EffectiveStmtCtx stmt, @NonNull BitsTypeDefinition base,
        @NonNull BitEffectiveStatement bit);

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBoolean(final Current<String, ?> ctx,
            final BooleanTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newBooleanBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createDecimal(final Current<String, ?> ctx,
            final DecimalTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final RangeRestrictedTypeBuilder<DecimalTypeDefinition, BigDecimal> builder =
                RestrictedTypes.newDecima64Builder(baseType, typeEffectiveQName(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RangeEffectiveStatement) {
                final RangeEffectiveStatement range = (RangeEffectiveStatement) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof FractionDigitsEffectiveStatement) {
                final Integer digits = ((FractionDigitsEffectiveStatement)stmt).argument();
                SourceException.throwIf(baseType.getFractionDigits() != digits, ctx,
                    "Cannot override fraction-digits from base type %s to %s", baseType, digits);
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createEmpty(final Current<String, ?> ctx,
            final EmptyTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newEmptyBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private @NonNull TypeEffectiveStatement<TypeStatement> createEnum(final Current<?, ?> ctx,
            final EnumTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final EnumerationTypeBuilder builder = RestrictedTypes.newEnumerationBuilder(baseType,
            ctx.argumentAsTypeQName());

        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof EnumEffectiveStatement) {
                builder.addEnum(addRestrictedEnum(ctx, baseType, (EnumEffectiveStatement) stmt));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    abstract @NonNull EnumPair addRestrictedEnum(@NonNull EffectiveStmtCtx stmt, @NonNull EnumTypeDefinition base,
        @NonNull EnumEffectiveStatement enumStmt);

    private static @NonNull TypeEffectiveStatement<TypeStatement> createIdentityref(final Current<String, ?> ctx,
            final IdentityrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newIdentityrefBuilder(baseType,
            typeEffectiveQName(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createInstanceIdentifier(final Current<String, ?> ctx,
            final InstanceIdentifierTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(baseType,
                    typeEffectiveQName(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)stmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static <T extends RangeRestrictedTypeDefinition<T, N>, N extends Number & Comparable<N>>
        @NonNull TypeEffectiveStatement<TypeStatement> createIntegral(final Current<?, ?> ctx,
                final TypeStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final RangeRestrictedTypeBuilder<T, N> builder) {
        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RangeEffectiveStatement) {
                final RangeEffectiveStatement rangeStmt = (RangeEffectiveStatement)stmt;
                builder.setRangeConstraint(rangeStmt, rangeStmt.argument());
            }
        }

        try {
            return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
        } catch (InvalidRangeConstraintException e) {
            throw new SourceException(ctx, e, "Invalid range constraint: %s", e.getOffendingRanges());
        }
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createLeafref(final Current<String, ?> ctx,
            final LeafrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> builder =
                RestrictedTypes.newLeafrefBuilder(baseType, AbstractTypeStatementSupport.typeEffectiveQName(ctx));

        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement) stmt).argument());
            }
        }
        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createString(final Current<String, ?> ctx,
            final StringTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StringTypeBuilder builder = RestrictedTypes.newStringBuilder(baseType,
            AbstractTypeStatementSupport.typeEffectiveQName(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof LengthEffectiveStatement) {
                final LengthEffectiveStatement length = (LengthEffectiveStatement)stmt;

                try {
                    builder.setLengthConstraint(length, length.argument());
                } catch (IllegalStateException e) {
                    throw new SourceException(ctx, e, "Multiple length constraints encountered");
                } catch (InvalidLengthConstraintException e) {
                    throw new SourceException(ctx, e, "Invalid length constraint %s", length.argument());
                }
            }
            if (stmt instanceof PatternEffectiveStatement) {
                builder.addPatternConstraint((PatternEffectiveStatement) stmt);
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createUnion(final Current<String, ?> ctx,
            final UnionTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newUnionBuilder(baseType,
            typeEffectiveQName(ctx)));
    }
}