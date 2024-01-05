/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalArgumentCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

final class TypeDefinitionAwareCodecTestHelper {
    private TypeDefinitionAwareCodecTestHelper() {
        // Hidden on purpose
    }

    static <T> T getCodec(final TypeDefinition<?> def, final Class<T> clazz) {
        return assertInstanceOf(clazz, TypeDefinitionAwareCodec.fromType(def));
    }

    static void deserializeWithExpectedIllegalArgEx(final IllegalArgumentCodec<String, ?> codec,
            final @NonNull String param) {
        assertThrows(IllegalArgumentException.class, () -> codec.deserialize(param));
    }

    static EnumTypeDefinition toEnumTypeDefinition(final String... enums) {
        final var b = BaseTypes.enumerationTypeBuilder(QName.create("foo", "foo"));
        var val = 0;
        for (final var en : enums) {
            final var mockEnum = mock(EnumPair.class);
            doReturn(en).when(mockEnum).getName();
            doReturn(val).when(mockEnum).getValue();
            b.addEnum(mockEnum);
            val++;
        }

        return b.build();
    }
}
