/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int8EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
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

            // FIXME: improve the test of isExtended - e.g. unknown statements, etc..
            Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = ctx.declaredSubstatements();
            boolean isExtended = !declaredSubstatements.isEmpty();
            if (isExtended) {
                return new ExtendedTypeEffectiveStatementImpl(ctx, true);
            }

            switch (ctx.getStatementArgument()) {
            case TypeUtils.INT8:
                return Int8EffectiveStatementImpl.getInstance();
            case TypeUtils.INT16:
                return Int16EffectiveStatementImpl.getInstance();
            case TypeUtils.INT32:
                return Int32EffectiveStatementImpl.getInstance();
            case TypeUtils.INT64:
                return Int64EffectiveStatementImpl.getInstance();
            case TypeUtils.UINT8:
                return UInt8EffectiveStatementImpl.getInstance();
            case TypeUtils.UINT16:
                return UInt16EffectiveStatementImpl.getInstance();
            case TypeUtils.UINT32:
                return UInt32EffectiveStatementImpl.getInstance();
            case TypeUtils.UINT64:
                return UInt64EffectiveStatementImpl.getInstance();
            case TypeUtils.STRING:
                return StringEffectiveStatementImpl.getInstance();
            case TypeUtils.BOOLEAN:
                return BooleanEffectiveStatementImpl.getInstance();
            case TypeUtils.EMPTY:
                return EmptyEffectiveStatementImpl.getInstance();
            case TypeUtils.BINARY:
                return BinaryEffectiveStatementImpl.getInstance();
            default:
                // FIXME: try to resolve original typedef context here and
                // return buildEffective of original typedef context
                return new ExtendedTypeEffectiveStatementImpl(ctx, false);
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
