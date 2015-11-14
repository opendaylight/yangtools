/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
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
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.DecimalEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IntegerEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnsignedIntegerTypeEffectiveStatement;

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
        public EffectiveStatement<String, TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            // First look up the proper base type
            final TypeDefinition<?> baseType;
            switch (ctx.getStatementArgument()) {
            case TypeUtils.BINARY:
                baseType = BaseTypes.binaryType();
                break;
            case TypeUtils.BOOLEAN:
                baseType = BaseTypes.booleanType();
                break;
            case TypeUtils.EMPTY:
                baseType = BaseTypes.emptyType();
                break;
            case "instance-identifier":
                baseType = BaseTypes.instanceIdentifierType();
                break;
            case TypeUtils.INT8:
                baseType = BaseTypes.int8Type();
                break;
            case TypeUtils.INT16:
                baseType = BaseTypes.int16Type();
                break;
            case TypeUtils.INT32:
                baseType = BaseTypes.int32Type();
                break;
            case TypeUtils.INT64:
                baseType = BaseTypes.int64Type();
                break;
            case TypeUtils.STRING:
                baseType = BaseTypes.stringType();
                break;
            case TypeUtils.UINT8:
                baseType = BaseTypes.uint8Type();
                break;
            case TypeUtils.UINT16:
                baseType = BaseTypes.uint16Type();
                break;
            case TypeUtils.UINT32:
                baseType = BaseTypes.uint32Type();
                break;
            case TypeUtils.UINT64:
                baseType = BaseTypes.uint64Type();
                break;
            default:
                final QName qname = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
                final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedef =
                        ctx.getFromNamespace(TypeNamespace.class, qname);
                if (typedef == null) {
                    throw new SourceException("Type definition of " + ctx.getStatementArgument() + "  not found",
                        ctx.getStatementSourceReference());
                }

                baseType = typedef.buildEffective().getTypeDefinition();
            }

            // Now instantiate the proper effective statement for that type
            if (baseType instanceof BinaryTypeDefinition) {
                return new BinaryEffectiveStatementImpl(ctx, (BinaryTypeDefinition) baseType);
            } else if (baseType instanceof BitsTypeDefinition) {
                return new BitsEffectiveStatementImpl(ctx, (BitsTypeDefinition) baseType);
            } else if (baseType instanceof BooleanTypeDefinition) {
                return new BooleanEffectiveStatementImpl(ctx, (BooleanTypeDefinition) baseType);
            } else if (baseType instanceof DecimalTypeDefinition) {
                return new DecimalEffectiveStatementImpl(ctx, (DecimalTypeDefinition) baseType);
            } else if (baseType instanceof EmptyTypeDefinition) {
                return new EmptyEffectiveStatementImpl(ctx, (EmptyTypeDefinition) baseType);
            } else if (baseType instanceof EnumTypeDefinition) {
                return new EnumTypeEffectiveStatement(ctx, baseType);
            } else if (baseType instanceof IdentityrefTypeDefinition) {
                return new IdentityrefTypeEffectiveStatement(ctx, baseType);
            } else if (baseType instanceof InstanceIdentifierTypeDefinition) {
                return new InstanceIdentifierEffectiveStatement(ctx, baseType);
            } else if (baseType instanceof IntegerTypeDefinition) {
                return new IntegerEffectiveStatementImpl(ctx, (IntegerTypeDefinition) baseType);
            } else if (baseType instanceof LeafrefTypeDefinition) {
                return new LeafrefTypeEffectiveStatement(ctx, baseType);
            } else if (baseType instanceof StringTypeDefinition) {
                return new StringEffectiveStatementImpl(ctx, (StringTypeDefinition) baseType);
            } else if (baseType instanceof UnionTypeDefinition) {
                return new UnionTypeEffectiveStatement(ctx, baseType);
            } else if (baseType instanceof UnsignedIntegerTypeDefinition) {
                return new UnsignedIntegerTypeEffectiveStatement(ctx, (UnsignedIntegerTypeDefinition) baseType);
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
