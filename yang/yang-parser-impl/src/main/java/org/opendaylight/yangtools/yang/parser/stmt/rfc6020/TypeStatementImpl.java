/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
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
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.TypeDefEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BuiltinEffectiveStatements;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.DecimalTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IdentityrefTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.InstanceIdentifierTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IntegerTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LeafrefTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnionTypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnsignedIntegerTypeEffectiveStatementImpl;

public class TypeStatementImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
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

    protected TypeStatementImpl(final StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    private static abstract class AbstractDefinition extends
        AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {
        AbstractDefinition() {
            super(YangStmtMapping..TYPE);
        }

        @Override
        public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }
    }

    public static class Definition extends AbstractDefinition {

        @Override
        public TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public TypeEffectiveStatement<TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            final QName qname = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
            final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedef =
                    ctx.getFromNamespace(TypeNamespace.class, qname);
            SourceException.throwIfNull(typedef, ctx.getStatementSourceReference(), "Type '%s' not found", qname);

            final TypedefEffectiveStatement effectiveTypedef = typedef.buildEffective();
            Verify.verify(effectiveTypedef instanceof TypeDefEffectiveStatementImpl);
            final TypeEffectiveStatement<TypeStatement> typeStmt =
                    ((TypeDefEffectiveStatementImpl) effectiveTypedef).asTypeEffectiveStatement();

            if (ctx.declaredSubstatements().isEmpty() && ctx.effectiveSubstatements().isEmpty()) {
                return typeStmt;
            }

            return createEffective(typeStmt.getTypeDefinition(), ctx);
        }

        @Override
        public void onFullDefinitionDeclared(
                final Mutable<String, TypeStatement, EffectiveStatement<String, TypeStatement>> stmt){

            final QName typeQName = Utils.qNameFromArgument(stmt, stmt.getStatementArgument());
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
                public void apply() {
                    // FIXME: This is not correct: statements should be validated based on the target type -- the following
                    //        is incorrent:
                    //        typedef foo { type string; }
                    //        typedef bar { type foo { fraction-digits 5; } }
                    // typePrereq.get() should get us to the right validator

                    SUBSTATEMENT_VALIDATOR.validate(stmt);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    InferenceException.throwIf(failed.contains(typePrereq), stmt.getStatementSourceReference(),
                        "Type [%s] was not found.", typeQName);
                }
            });
        }

        private static abstract class BuiltinDefinition extends AbstractDefinition {
            final EffectiveStatement<String, TypeStatement> effective;
            final TypeStatement declared;

            BuiltinDefinition(final TypeStatement declared, final EffectiveStatement<String, TypeStatement> effective) {
                this.declared = Preconditions.checkNotNull(declared);
                this.effective = Preconditions.checkNotNull(effective);
            }

            @Override
            public final TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
                if (ctx.declaredSubstatements().isEmpty() && ctx.effectiveSubstatements().isEmpty()) {
                    return declared;
                }

                return new TypeStatementImpl(ctx);
            }
            @Override
            public EffectiveStatement<String, TypeStatement> createEffective(
                    final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
                if (ctx.declaredSubstatements().isEmpty() && ctx.effectiveSubstatements().isEmpty()) {
                    return effective;
                }

                return effective(ctx);
            }

            abstract EffectiveStatement<String, TypeStatement> effective(
                    StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx);
        }

        private static final class IntegerDefinition extends BuiltinDefinition {

            IntegerDefinition(final TypeStatement declared, final EffectiveStatement<String, TypeStatement> effective) {
                super(declared, effective);
            }

            @Override
            final EffectiveStatement<String, TypeStatement> effective(
                    final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
                return new IntegerTypeEffectiveStatementImpl(ctx, (IntegerTypeDefinition) effective);
            }
        }

        private static final class UnsignedIntegerDefinition extends BuiltinDefinition {
            UnsignedIntegerDefinition(final TypeStatement declared,
                final EffectiveStatement<String, TypeStatement> effective) {
                super(declared, effective);
            }

            @Override
            final EffectiveStatement<String, TypeStatement> effective(
                    final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
                return new UnsignedIntegerTypeEffectiveStatementImpl(ctx, (UnsignedIntegerTypeDefinition) effective);
            }
        }

        private static final class StringDefinition extends BuiltinDefinition {
            StringDefinition() {
                super(BuiltinTypeStatement.STRING, BuiltinEffectiveStatements.STRING);
            }

            @Override
            final EffectiveStatement<String, TypeStatement> effective(
                    final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
                return new StringTypeEffectiveStatementImpl(ctx, (StringTypeDefinition) effective);
            }
        }

        private static final Map<String, StatementSupport<?, ?, ?>> BUILTIN_TYPES =
                ImmutableMap.<String, StatementSupport<?, ?, ?>>builder()

                // BINARY
                // EMPTY
                // INSTANCE_IDENTIFIER

                .put(TypeUtils.BITS, new BitsSpecificationImpl.Definition())
                .put(TypeUtils.DECIMAL64, new Decimal64SpecificationImpl.Definition())
                .put(TypeUtils.ENUMERATION, new EnumSpecificationImpl.Definition())
                .put(TypeUtils.IDENTITY_REF, new IdentityRefSpecificationImpl.Definition())
                .put(TypeUtils.INSTANCE_IDENTIFIER, new InstanceIdentifierSpecificationImpl.Definition())
                .put(TypeUtils.INT8, new IntegerDefinition(BuiltinTypeStatement.INT8,
                    BuiltinEffectiveStatements.INT8))
                .put(TypeUtils.INT16, new IntegerDefinition(BuiltinTypeStatement.INT16,
                    BuiltinEffectiveStatements.INT16))
                .put(TypeUtils.INT32, new IntegerDefinition(BuiltinTypeStatement.INT32,
                    BuiltinEffectiveStatements.INT32))
                .put(TypeUtils.INT64, new IntegerDefinition(BuiltinTypeStatement.INT64,
                    BuiltinEffectiveStatements.INT64))
                .put(TypeUtils.LEAF_REF, new LeafrefSpecificationImpl.Definition())
                .put(TypeUtils.UINT8, new StringDefinition())
                .put(TypeUtils.UINT8, new UnsignedIntegerDefinition(BuiltinTypeStatement.UINT8,
                    BuiltinEffectiveStatements.UINT8))
                .put(TypeUtils.UINT16, new UnsignedIntegerDefinition(BuiltinTypeStatement.UINT16,
                    BuiltinEffectiveStatements.UINT16))
                .put(TypeUtils.UINT32, new UnsignedIntegerDefinition(BuiltinTypeStatement.UINT32,
                    BuiltinEffectiveStatements.UINT32))
                .put(TypeUtils.UINT64, new UnsignedIntegerDefinition(BuiltinTypeStatement.UINT64,
                    BuiltinEffectiveStatements.UINT64))
                .put(TypeUtils.UNION, new UnionSpecificationImpl.Definition())
                .build();

        @Override
        public StatementSupport<?, ?, ?> getSupportForArgument(final String argumentValue) {
            final StatementSupport<?, ?, ?> builtin = BUILTIN_TYPES.get(argumentValue);
            return builtin != null ? builtin : super.getSupportForArgument(argumentValue);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        private static TypeEffectiveStatement<TypeStatement> createEffective(final TypeDefinition<?> baseType,
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
            // Now instantiate the proper effective statement for that type
            if (baseType instanceof BinaryTypeDefinition) {
                return new BinaryTypeEffectiveStatementImpl(ctx, (BinaryTypeDefinition) baseType);
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
                return new InstanceIdentifierTypeEffectiveStatementImpl(ctx, (InstanceIdentifierTypeDefinition) baseType);
            } else if (baseType instanceof IntegerTypeDefinition) {
                return new IntegerTypeEffectiveStatementImpl(ctx, (IntegerTypeDefinition) baseType);
            } else if (baseType instanceof LeafrefTypeDefinition) {
                return new LeafrefTypeEffectiveStatementImpl(ctx, (LeafrefTypeDefinition) baseType);
            } else if (baseType instanceof StringTypeDefinition) {
                return new StringTypeEffectiveStatementImpl(ctx, (StringTypeDefinition) baseType);
            } else if (baseType instanceof UnionTypeDefinition) {
                return new UnionTypeEffectiveStatementImpl(ctx, (UnionTypeDefinition) baseType);
            } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
                return new UnsignedIntegerTypeEffectiveStatementImpl(ctx, (UnsignedIntegerTypeDefinition) baseType);
            } else {
                throw new IllegalStateException("Unhandled base type " + baseType);
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
