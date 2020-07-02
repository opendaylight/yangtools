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
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.LengthRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
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

abstract class AbstractTypeStatementSupport
        extends BaseStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {
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
            return new BitsTypeEffectiveStatementImpl(ctx, (BitsTypeDefinition) baseType);
        } else if (baseType instanceof BooleanTypeDefinition) {
            return new BooleanTypeEffectiveStatementImpl(ctx, (BooleanTypeDefinition) baseType);
        } else if (baseType instanceof DecimalTypeDefinition) {
            return new DecimalTypeEffectiveStatementImpl(ctx, (DecimalTypeDefinition) baseType);
        } else if (baseType instanceof EmptyTypeDefinition) {
            return new EmptyTypeEffectiveStatementImpl(ctx, (EmptyTypeDefinition) baseType);
        } else if (baseType instanceof EnumTypeDefinition) {
            return new EnumTypeEffectiveStatementImpl(ctx, (EnumTypeDefinition) baseType);
        } else if (baseType instanceof IdentityrefTypeDefinition) {
            return new IdentityrefTypeEffectiveStatementImpl(ctx, (IdentityrefTypeDefinition) baseType);
        } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return new InstanceIdentifierTypeEffectiveStatementImpl(ctx,
                (InstanceIdentifierTypeDefinition) baseType);
        } else if (baseType instanceof Int8TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newInt8Builder((Int8TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int16TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newInt16Builder((Int16TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int32TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newInt32Builder((Int32TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Int64TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newInt64Builder((Int64TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof LeafrefTypeDefinition) {
            return new LeafrefTypeEffectiveStatementImpl(ctx, (LeafrefTypeDefinition) baseType);
        } else if (baseType instanceof StringTypeDefinition) {
            return new StringTypeEffectiveStatementImpl(ctx, (StringTypeDefinition) baseType);
        } else if (baseType instanceof Uint8TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newUint8Builder((Uint8TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint16TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newUint16Builder((Uint16TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint32TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newUint32Builder((Uint32TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof Uint64TypeDefinition) {
            return new IntegralTypeEffectiveStatementImpl<>(ctx,
                    RestrictedTypes.newUint64Builder((Uint64TypeDefinition) baseType, typeEffectiveSchemaPath(ctx)));
        } else if (baseType instanceof UnionTypeDefinition) {
            return new UnionTypeEffectiveStatementImpl(ctx, (UnionTypeDefinition) baseType);
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
}