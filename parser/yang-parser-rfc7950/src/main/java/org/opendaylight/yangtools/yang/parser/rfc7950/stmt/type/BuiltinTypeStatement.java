/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithQNameArgument;

final class BuiltinTypeStatement extends WithQNameArgument implements TypeStatement {
    private static final ImmutableMap<String, BuiltinTypeStatement> BUILTINS;

    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, BuiltInType.BINARY);
        putBuiltin(builder, BuiltInType.BOOLEAN);
        putBuiltin(builder, BuiltInType.EMPTY);
        putBuiltin(builder, BuiltInType.INSTANCE_IDENTIFIER);
        putBuiltin(builder, BuiltInType.INT8);
        putBuiltin(builder, BuiltInType.INT16);
        putBuiltin(builder, BuiltInType.INT32);
        putBuiltin(builder, BuiltInType.INT64);
        putBuiltin(builder, BuiltInType.STRING);
        putBuiltin(builder, BuiltInType.UINT8);
        putBuiltin(builder, BuiltInType.UINT16);
        putBuiltin(builder, BuiltInType.UINT32);
        putBuiltin(builder, BuiltInType.UINT64);
        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final BuiltInType type) {
        final var argument = type.typeName();
        builder.put(argument.getLocalName(), new BuiltinTypeStatement(argument));
    }

    private BuiltinTypeStatement(final QName argument) {
        super(argument);
    }

    static @Nullable TypeStatement lookup(final String rawArgument) {
        return BUILTINS.get(rawArgument);
    }
}
