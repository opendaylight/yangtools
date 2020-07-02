/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class BuiltinTypeStatement extends WithRawStringArgument implements TypeStatement {
    private static final ImmutableMap<String, BuiltinTypeStatement> BUILTINS;

    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, AbstractTypeStatementSupport.BINARY);
        putBuiltin(builder, AbstractTypeStatementSupport.BOOLEAN);
        putBuiltin(builder, AbstractTypeStatementSupport.EMPTY);
        putBuiltin(builder, AbstractTypeStatementSupport.INSTANCE_IDENTIFIER);
        putBuiltin(builder, AbstractTypeStatementSupport.INT8);
        putBuiltin(builder, AbstractTypeStatementSupport.INT16);
        putBuiltin(builder, AbstractTypeStatementSupport.INT32);
        putBuiltin(builder, AbstractTypeStatementSupport.INT64);
        putBuiltin(builder, AbstractTypeStatementSupport.STRING);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT8);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT16);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT32);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT64);
        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final String argument) {
        builder.put(argument, new BuiltinTypeStatement(argument));
    }

    private BuiltinTypeStatement(final String rawArgument) {
        super(requireNonNull(rawArgument));
    }

    static @Nullable TypeStatement lookup(final StmtContext<String, TypeStatement, ?> ctx) {
        return BUILTINS.get(ctx.coerceStatementArgument());
    }
}
