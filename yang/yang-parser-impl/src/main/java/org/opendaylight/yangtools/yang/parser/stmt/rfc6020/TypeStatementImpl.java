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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int8EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.TypeEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt8EffectiveStatementImpl;

public class TypeStatementImpl extends AbstractDeclaredStatement<String>
        implements TypeStatement {

    protected TypeStatementImpl(final StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public TypeStatement createDeclared(final StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement> createEffective(
                final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            // Look up the potential typedef
            final QName qname = Utils.qNameFromArgument(ctx, ctx.getStatementArgument());
            StmtContext<?, TypedefStatement, TypedefEffectiveStatement> typedefCtx =
                    ctx.getFromNamespace(TypeNamespace.class, qname);

            final TypedefEffectiveStatement typedef;
            if (typedefCtx == null) {
                // Lookup default YANG types
                switch (ctx.getStatementArgument()) {
                case TypeUtils.INT8:
                    typedef = Int8EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.INT16:
                    typedef = Int16EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.INT32:
                    typedef = Int32EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.INT64:
                    typedef = Int64EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.UINT8:
                    typedef = UInt8EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.UINT16:
                    typedef = UInt16EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.UINT32:
                    typedef = UInt32EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.UINT64:
                    typedef = UInt64EffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.STRING:
                    typedef = StringEffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.BOOLEAN:
                    typedef = BooleanEffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.EMPTY:
                    typedef = EmptyEffectiveStatementImpl.getInstance();
                    break;
                case TypeUtils.BINARY:
                    typedef = BinaryEffectiveStatementImpl.getInstance();
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Failed to look up base type for %s at %s", qname,
                        ctx.getStatementSourceReference()));
                }
            } else {
                typedef = typedefCtx.buildEffective();
            }

            return new TypeEffectiveStatementImpl(ctx, typedef);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
