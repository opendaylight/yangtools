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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithQNameArgument;

final class BuiltinTypeStatement extends WithQNameArgument implements TypeStatement {
    private static final ImmutableMap<String, BuiltinTypeStatement> BUILTINS;

    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, TypeDefinitions.TYPEDEF_BINARY);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_BOOLEAN);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_EMPTY);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_INSTANCE_IDENTIFIER);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_INT8);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_INT16);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_INT32);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_INT64);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_STRING);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_UINT8);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_UINT16);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_UINT32);
        putBuiltin(builder, TypeDefinitions.TYPEDEF_UINT64);
        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final QName argument) {
        builder.put(argument.getLocalName(), new BuiltinTypeStatement(argument));
    }

    private BuiltinTypeStatement(final QName argument) {
        super(argument);
    }

    static @Nullable TypeStatement lookup(final String rawArgument) {
        return BUILTINS.get(rawArgument);
    }
}
