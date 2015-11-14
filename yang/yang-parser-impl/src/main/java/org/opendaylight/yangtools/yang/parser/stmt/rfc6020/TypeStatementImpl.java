/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Verify;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
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
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
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

    protected TypeStatementImpl(final StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value)
                throws SourceException {
            return value;
        }

        @Override
        public TypeStatement createDeclared(
                final StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public TypeEffectiveStatement<TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            // First look up the proper base type
            final TypeEffectiveStatement<TypeStatement> typeStmt;
            switch (ctx.getStatementArgument()) {
            case TypeUtils.BINARY:
                typeStmt = BuiltinEffectiveStatements.BINARY;
                break;
            case TypeUtils.BOOLEAN:
                typeStmt = BuiltinEffectiveStatements.BOOLEAN;
                break;
            case TypeUtils.EMPTY:
                typeStmt = BuiltinEffectiveStatements.EMPTY;
                break;
            case TypeUtils.INSTANCE_IDENTIFIER:
                typeStmt = BuiltinEffectiveStatements.INSTANCE_IDENTIFIER;
                break;
            case TypeUtils.INT8:
                typeStmt = BuiltinEffectiveStatements.INT8;
                break;
            case TypeUtils.INT16:
                typeStmt = BuiltinEffectiveStatements.INT16;
                break;
            case TypeUtils.INT32:
                typeStmt = BuiltinEffectiveStatements.INT32;
                break;
            case TypeUtils.INT64:
                typeStmt = BuiltinEffectiveStatements.INT64;
                break;
            case TypeUtils.STRING:
                typeStmt = BuiltinEffectiveStatements.STRING;
                break;
            case TypeUtils.UINT8:
                typeStmt = BuiltinEffectiveStatements.UINT8;
                break;
            case TypeUtils.UINT16:
                typeStmt = BuiltinEffectiveStatements.UINT16;
                break;
            case TypeUtils.UINT32:
                typeStmt = BuiltinEffectiveStatements.UINT32;
                break;
            case TypeUtils.UINT64:
                typeStmt = BuiltinEffectiveStatements.UINT64;
                break;
            default:
                final QName qname = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
                final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedef =
                        ctx.getFromNamespace(TypeNamespace.class, qname);
                if (typedef == null) {
                    throw new SourceException("Type '" + qname + "' not found", ctx.getStatementSourceReference());
                }

                final TypedefEffectiveStatement effectiveTypedef = typedef.buildEffective();
                Verify.verify(effectiveTypedef instanceof TypeDefEffectiveStatementImpl);
                typeStmt = ((TypeDefEffectiveStatementImpl) effectiveTypedef).asTypeEffectiveStatement();
            }

            if (ctx.declaredSubstatements().isEmpty() && ctx.effectiveSubstatements().isEmpty()) {
                return typeStmt;
            }

            // Now instantiate the proper effective statement for that type
            final TypeDefinition<?> baseType = typeStmt.getTypeDefinition();
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
