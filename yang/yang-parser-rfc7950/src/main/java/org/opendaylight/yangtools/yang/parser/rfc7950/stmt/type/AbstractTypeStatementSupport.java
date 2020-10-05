/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
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
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.InstanceIdentifierTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.InvalidRangeConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.LengthRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RequireInstanceRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.util.type.StringTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTypeStatementSupport
        extends BaseStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeStatementSupport.class);

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

    private static final ImmutableMap<String, String> BUILT_IN_TYPES = ImmutableMap.<String, String>builder()
        .put(BINARY, BINARY)
        .put(BITS, BITS)
        .put(BOOLEAN, BOOLEAN)
        .put(DECIMAL64, DECIMAL64)
        .put(EMPTY, EMPTY)
        .put(ENUMERATION, ENUMERATION)
        .put(IDENTITY_REF,IDENTITY_REF)
        .put(INSTANCE_IDENTIFIER, INSTANCE_IDENTIFIER)
        .put(INT8, INT8)
        .put(INT16, INT16)
        .put(INT32, INT32)
        .put(INT64, INT64)
        .put(LEAF_REF, LEAF_REF)
        .put(STRING, STRING)
        .put(UINT8, UINT8)
        .put(UINT16, UINT16)
        .put(UINT32, UINT32)
        .put(UINT64, UINT64)
        .put(UNION, UNION)
        .build();

    private static final ImmutableMap<String, StatementSupport<?, ?, ?>> ARGUMENT_SPECIFIC_SUPPORTS =
            ImmutableMap.<String, StatementSupport<?, ?, ?>>builder()
            .put(BITS, new BitsSpecificationSupport())
            .put(DECIMAL64, new Decimal64SpecificationSupport())
            .put(ENUMERATION, new EnumSpecificationSupport())
            .put(IDENTITY_REF, new IdentityRefSpecificationRFC6020Support())
            .put(INSTANCE_IDENTIFIER, new InstanceIdentifierSpecificationSupport())
            .put(LEAF_REF, new LeafrefSpecificationRFC6020Support())
            .put(UNION, new UnionSpecificationSupport())
            .build();

    AbstractTypeStatementSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<String, TypeStatement, EffectiveStatement<String, TypeStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        // if it is yang built-in type, no prerequisite is needed, so simply return
        if (BUILT_IN_TYPES.containsKey(stmt.getStatementArgument())) {
            return;
        }

        final QName typeQName = StmtContextUtils.parseNodeIdentifier(stmt, stmt.getStatementArgument());
        final ModelActionBuilder typeAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<StmtContext<?, ?, ?>> typePrereq = typeAction.requiresCtx(stmt, TypeNamespace.class,
                typeQName, ModelProcessingPhase.EFFECTIVE_MODEL);
        typeAction.mutatesEffectiveCtx(stmt.getParentContext());

        /*
         * If the type does not exist, throw new InferenceException.
         * Otherwise perform no operation.
         */
        typeAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                // Intentional NOOP
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(typePrereq), stmt.getStatementSourceReference(),
                    "Type [%s] was not found.", typeQName);
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
        return !ARGUMENT_SPECIFIC_SUPPORTS.isEmpty();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        return ARGUMENT_SPECIFIC_SUPPORTS.get(argument);
    }

    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected final TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularTypeStatement(ctx, substatements);
    }

    @Override
    protected final TypeStatement createEmptyDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
        final TypeStatement builtin;
        return (builtin = BuiltinTypeStatement.lookup(ctx)) != null ? builtin : new EmptyTypeStatement(ctx);
    }

    @Override
    protected final TypeEffectiveStatement<TypeStatement> createEffective(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final TypeStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // First look up the proper base type
        final TypeEffectiveStatement<TypeStatement> typeStmt = resolveType(ctx);
        // Now instantiate the proper effective statement for that type
        final TypeDefinition<?> baseType = typeStmt.getTypeDefinition();
        if (baseType instanceof BinaryTypeDefinition) {
            return createBinary(ctx, (BinaryTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof BitsTypeDefinition) {
            return createBits(ctx, (BitsTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return createBoolean(ctx, (BooleanTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return createDecimal(ctx, (DecimalTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return createEmpty(ctx, (EmptyTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof EnumTypeDefinition) {
            return createEnum(ctx, (EnumTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return createIdentityref(ctx, (IdentityrefTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return createInstanceIdentifier(ctx, (InstanceIdentifierTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof Int8TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                RestrictedTypes.newInt8Builder((Int8TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int16TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newInt16Builder((Int16TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int32TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newInt32Builder((Int32TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int64TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newInt64Builder((Int64TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return createLeafref(ctx, (LeafrefTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof StringTypeDefinition) {
            return createString(ctx, (StringTypeDefinition) baseType, declared, substatements);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newUint8Builder((Uint8TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint16TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newUint16Builder((Uint16TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint32TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newUint32Builder((Uint32TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint64TypeDefinition) {
            return createIntegral(ctx, declared, substatements,
                    RestrictedTypes.newUint64Builder((Uint64TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof UnionTypeDefinition) {
            return createUnion(ctx, (UnionTypeDefinition) baseType, declared, substatements);
        } else {
            throw new IllegalStateException("Unhandled base type " + baseType);
        }
    }

    @Override
    protected final EffectiveStatement<String, TypeStatement> createEmptyEffective(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final TypeStatement declared) {
        return resolveType(ctx);
    }

    static final SchemaPath typeEffectiveSchemaPath(final StmtContext<?, ?, ?> stmtCtx) {
        final SchemaPath path = stmtCtx.getSchemaPath().get();
        final SchemaPath parent = path.getParent();
        final QName parentQName = parent.getLastComponent();
        checkArgument(parentQName != null, "Path %s has an empty parent", path);

        final QName qname = path.getLastComponent().bindTo(parentQName.getModule()).intern();
        return parent.createChild(qname);
    }

    /**
     * Resolve type reference, as pointed to by the context's argument.
     *
     * @param ctx Statement context
     * @return Resolved type
     * @throws SourceException if the target type cannot be found
     */
    private static @NonNull TypeEffectiveStatement<TypeStatement> resolveType(final StmtContext<String, ?, ?> ctx) {
        final String argument = ctx.coerceStatementArgument();
        switch (argument) {
            case BINARY:
                return BuiltinEffectiveStatement.BINARY;
            case BOOLEAN:
                return BuiltinEffectiveStatement.BOOLEAN;
            case EMPTY:
                return BuiltinEffectiveStatement.EMPTY;
            case INSTANCE_IDENTIFIER:
                return BuiltinEffectiveStatement.INSTANCE_IDENTIFIER;
            case INT8:
                return BuiltinEffectiveStatement.INT8;
            case INT16:
                return BuiltinEffectiveStatement.INT16;
            case INT32:
                return BuiltinEffectiveStatement.INT32;
            case INT64:
                return BuiltinEffectiveStatement.INT64;
            case STRING:
                return BuiltinEffectiveStatement.STRING;
            case UINT8:
                return BuiltinEffectiveStatement.UINT8;
            case UINT16:
                return BuiltinEffectiveStatement.UINT16;
            case UINT32:
                return BuiltinEffectiveStatement.UINT32;
            case UINT64:
                return BuiltinEffectiveStatement.UINT64;
            default:
                final QName qname = StmtContextUtils.parseNodeIdentifier(ctx, argument);
                final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedef =
                        SourceException.throwIfNull(ctx.getFromNamespace(TypeNamespace.class, qname),
                            ctx.getStatementSourceReference(), "Type '%s' not found", qname);
                return typedef.buildEffective().asTypeEffectiveStatement();
        }
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBinary(final StmtContext<?, ?, ?> ctx,
            final BinaryTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final LengthRestrictedTypeBuilder<BinaryTypeDefinition> builder =
                RestrictedTypes.newBinaryBuilder(baseType, typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof LengthEffectiveStatement) {
                final LengthEffectiveStatement length = (LengthEffectiveStatement)stmt;

                try {
                    builder.setLengthConstraint(length, length.argument());
                } catch (IllegalStateException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e,
                        "Multiple length constraints encountered");
                } catch (InvalidLengthConstraintException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid length constraint %s",
                        length.argument());
                }
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBits(final StmtContext<?, ?, ?> ctx,
            final BitsTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final BitsTypeBuilder builder = RestrictedTypes.newBitsBuilder(baseType, ctx.getSchemaPath().get());

        final YangVersion yangVersion = ctx.getRootVersion();
        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof BitEffectiveStatement) {
                SourceException.throwIf(yangVersion != YangVersion.VERSION_1_1, ctx.getStatementSourceReference(),
                        "Restricted bits type is allowed only in YANG 1.1 version.");
                final BitEffectiveStatement bitSubStmt = (BitEffectiveStatement) stmt;

                // FIXME: this looks like a duplicate of BitsSpecificationEffectiveStatement
                final Optional<Uint32> declaredPosition = bitSubStmt.getDeclaredPosition();
                final Uint32 effectivePos;
                if (declaredPosition.isEmpty()) {
                    effectivePos = getBaseTypeBitPosition(bitSubStmt.argument(), baseType, ctx);
                } else {
                    effectivePos = declaredPosition.get();
                }

                builder.addBit(EffectiveTypeUtil.buildBit(bitSubStmt, effectivePos));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createBoolean(final StmtContext<?, ?, ?> ctx,
            final BooleanTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newBooleanBuilder(baseType,
            typeEffectiveSchemaPath(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createDecimal(final StmtContext<?, ?, ?> ctx,
            final DecimalTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final RangeRestrictedTypeBuilder<DecimalTypeDefinition, BigDecimal> builder =
                RestrictedTypes.newDecima64Builder(baseType, typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RangeEffectiveStatement) {
                final RangeEffectiveStatement range = (RangeEffectiveStatement) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof FractionDigitsEffectiveStatement) {
                final Integer digits = ((FractionDigitsEffectiveStatement)stmt).argument();
                SourceException.throwIf(baseType.getFractionDigits() != digits, ctx.getStatementSourceReference(),
                    "Cannot override fraction-digits from base type %s to %s", baseType, digits);
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createEmpty(final StmtContext<?, ?, ?> ctx,
            final EmptyTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newEmptyBuilder(baseType,
            typeEffectiveSchemaPath(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createEnum(final StmtContext<?, ?, ?> ctx,
            final EnumTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final EnumerationTypeBuilder builder = RestrictedTypes.newEnumerationBuilder(baseType,
            ctx.getSchemaPath().get());

        final YangVersion yangVersion = ctx.getRootVersion();
        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof EnumEffectiveStatement) {
                SourceException.throwIf(yangVersion != YangVersion.VERSION_1_1, ctx.getStatementSourceReference(),
                        "Restricted enumeration type is allowed only in YANG 1.1 version.");

                final EnumEffectiveStatement enumSubStmt = (EnumEffectiveStatement) stmt;
                final Optional<Integer> declaredValue =
                        enumSubStmt.findFirstEffectiveSubstatementArgument(ValueEffectiveStatement.class);
                final int effectiveValue;
                if (declaredValue.isEmpty()) {
                    effectiveValue = getBaseTypeEnumValue(enumSubStmt.getDeclared().rawArgument(), baseType, ctx);
                } else {
                    effectiveValue = declaredValue.orElseThrow();
                }

                builder.addEnum(EffectiveTypeUtil.buildEnumPair(enumSubStmt, effectiveValue));
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createIdentityref(final StmtContext<?, ?, ?> ctx,
            final IdentityrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newIdentityrefBuilder(baseType,
            typeEffectiveSchemaPath(ctx)));
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createInstanceIdentifier(
            final StmtContext<?, ?, ?> ctx, final InstanceIdentifierTypeDefinition baseType,
            final TypeStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(baseType,
                    typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)stmt).argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static <T extends RangeRestrictedTypeDefinition<T, N>, N extends Number & Comparable<N>>
        @NonNull TypeEffectiveStatement<TypeStatement> createIntegral(final StmtContext<?, ?, ?> ctx,
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
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid range constraint: %s",
                e.getOffendingRanges());
        }
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createLeafref(final StmtContext<?, ?, ?> ctx,
            final LeafrefTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> builder =
                RestrictedTypes.newLeafrefBuilder(baseType, AbstractTypeStatementSupport.typeEffectiveSchemaPath(ctx));

        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement) stmt).argument());
            }
        }
        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createString(final StmtContext<?, ?, ?> ctx,
            final StringTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StringTypeBuilder builder = RestrictedTypes.newStringBuilder(baseType,
            AbstractTypeStatementSupport.typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof LengthEffectiveStatement) {
                final LengthEffectiveStatement length = (LengthEffectiveStatement)stmt;

                try {
                    builder.setLengthConstraint(length, length.argument());
                } catch (IllegalStateException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e,
                            "Multiple length constraints encountered");
                } catch (InvalidLengthConstraintException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid length constraint %s",
                        length.argument());
                }
            }
            if (stmt instanceof PatternEffectiveStatement) {
                builder.addPatternConstraint((PatternEffectiveStatement) stmt);
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    private static @NonNull TypeEffectiveStatement<TypeStatement> createUnion(final StmtContext<?, ?, ?> ctx,
            final UnionTypeDefinition baseType, final TypeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new TypeEffectiveStatementImpl<>(declared, substatements, RestrictedTypes.newUnionBuilder(baseType,
            typeEffectiveSchemaPath(ctx)));
    }

    private static Uint32 getBaseTypeBitPosition(final String bitName, final BitsTypeDefinition baseType,
            final StmtContext<?, ?, ?> ctx) {
        for (Bit baseTypeBit : baseType.getBits()) {
            if (bitName.equals(baseTypeBit.getName())) {
                return baseTypeBit.getPosition();
            }
        }

        throw new SourceException(ctx.getStatementSourceReference(),
                "Bit '%s' is not a subset of its base bits type %s.", bitName, baseType.getQName());
    }

    private static int getBaseTypeEnumValue(final String enumName, final EnumTypeDefinition baseType,
            final StmtContext<?, ?, ?> ctx) {
        for (EnumPair baseTypeEnumPair : baseType.getValues()) {
            if (enumName.equals(baseTypeEnumPair.getName())) {
                return baseTypeEnumPair.getValue();
            }
        }

        throw new SourceException(ctx.getStatementSourceReference(),
                "Enum '%s' is not a subset of its base enumeration type %s.", enumName, baseType.getQName());
    }
}