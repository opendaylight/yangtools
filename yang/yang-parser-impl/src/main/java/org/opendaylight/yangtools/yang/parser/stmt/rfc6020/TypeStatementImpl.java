/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;

import java.util.Collection;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ExtendedTypeEffectiveStatementImpl;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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

    protected TypeStatementImpl(StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value)
                throws SourceException {
            return value;
        }

        @Override
        public TypeStatement createDeclared(
                StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement> createEffective(
                StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            // :FIXME improve the test of isExtended - e.g. unknown statements,
            // etc..
            Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = ctx
                    .declaredSubstatements();
            boolean isExtended = declaredSubstatements.isEmpty() ? false
                    : true;
            if (isExtended)
                return new ExtendedTypeEffectiveStatementImpl(ctx, true);

            switch (ctx.getStatementArgument()) {
            case TypeUtils.INT8:
                return new Int8EffectiveStatementImpl(ctx);
            case TypeUtils.INT16:
                return new Int16EffectiveStatementImpl(ctx);
            case TypeUtils.INT32:
                return new Int32EffectiveStatementImpl(ctx);
            case TypeUtils.INT64:
                return new Int64EffectiveStatementImpl(ctx);
            case TypeUtils.UINT8:
                return new UInt8EffectiveStatementImpl(ctx);
            case TypeUtils.UINT16:
                return new UInt16EffectiveStatementImpl(ctx);
            case TypeUtils.UINT32:
                return new UInt32EffectiveStatementImpl(ctx);
            case TypeUtils.UINT64:
                return new UInt64EffectiveStatementImpl(ctx);
            case TypeUtils.STRING:
                return new StringEffectiveStatementImpl(ctx);
            case TypeUtils.BOOLEAN:
                return new BooleanEffectiveStatementImpl(ctx);
            case TypeUtils.EMPTY:
                return new EmptyEffectiveStatementImpl(ctx);
            case TypeUtils.BINARY:
                return new BinaryEffectiveStatementImpl(ctx);
            default:
                // :FIXME try to resolve original typedef context here and
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
