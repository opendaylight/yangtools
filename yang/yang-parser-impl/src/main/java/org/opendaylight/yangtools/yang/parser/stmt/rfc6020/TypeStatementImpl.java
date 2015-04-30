/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.TypeEffectiveStatementImpl;
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

public class TypeStatementImpl extends AbstractDeclaredStatement<String> implements TypeStatement {

    protected TypeStatementImpl(StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, TypeStatement, EffectiveStatement<String, TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public TypeStatement createDeclared(StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement> createEffective(
                StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {

            switch (ctx.getStatementArgument()) {
            case Int8EffectiveStatementImpl.LOCAL_NAME:
                return new Int8EffectiveStatementImpl(ctx);
            case Int16EffectiveStatementImpl.LOCAL_NAME:
                return new Int16EffectiveStatementImpl(ctx);
            case Int32EffectiveStatementImpl.LOCAL_NAME:
                return new Int32EffectiveStatementImpl(ctx);
            case Int64EffectiveStatementImpl.LOCAL_NAME:
                return new Int64EffectiveStatementImpl(ctx);
            case UInt8EffectiveStatementImpl.LOCAL_NAME:
                return new UInt8EffectiveStatementImpl(ctx);
            case UInt16EffectiveStatementImpl.LOCAL_NAME:
                return new UInt16EffectiveStatementImpl(ctx);
            case UInt32EffectiveStatementImpl.LOCAL_NAME:
                return new UInt32EffectiveStatementImpl(ctx);
            case UInt64EffectiveStatementImpl.LOCAL_NAME:
                return new UInt64EffectiveStatementImpl(ctx);
            case StringEffectiveStatementImpl.LOCAL_NAME:
                return new StringEffectiveStatementImpl(ctx);
            case BooleanEffectiveStatementImpl.LOCAL_NAME:
                return new BooleanEffectiveStatementImpl(ctx);
            case EmptyEffectiveStatementImpl.LOCAL_NAME:
                return new EmptyEffectiveStatementImpl(ctx);
            default:
                return new TypeEffectiveStatementImpl(ctx);
            }
        }

    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }
}
