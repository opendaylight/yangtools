/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.base.VerifyException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithQNameArgument;

@NonNullByDefault
final class BuiltinTypeStatement extends WithQNameArgument implements TypeStatement {
    private static final Map<String, BuiltinTypeStatement> BUILTINS =
        Stream.of(BuiltInType.BINARY, BuiltInType.BOOLEAN, BuiltInType.EMPTY, BuiltInType.INSTANCE_IDENTIFIER,
            BuiltInType.INT8, BuiltInType.INT16, BuiltInType.INT32, BuiltInType.INT64,
            BuiltInType.STRING,
            BuiltInType.UINT8, BuiltInType.UINT16, BuiltInType.UINT32, BuiltInType.UINT64)
        .map(BuiltInType::typeName)
        .collect(Collectors.toUnmodifiableMap(QName::getLocalName, BuiltinTypeStatement::new));

    static {
        if (BUILTINS.size() != 13) {
            throw new VerifyException("Unexpected built-ins " + BUILTINS);
        }
    }

    private BuiltinTypeStatement(final QName argument) {
        super(argument);
    }

    static @Nullable TypeStatement lookup(final String rawArgument) {
        return BUILTINS.get(rawArgument);
    }
}
