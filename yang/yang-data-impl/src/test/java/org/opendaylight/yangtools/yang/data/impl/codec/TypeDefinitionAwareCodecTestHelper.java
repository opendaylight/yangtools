/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumerationTypeBuilder;

public final class TypeDefinitionAwareCodecTestHelper {
    private TypeDefinitionAwareCodecTestHelper() {
        // Hidden on purpose
    }

    public static <T> T getCodec(final TypeDefinition<?> def, final Class<T> clazz) {
        TypeDefinitionAwareCodec<?, ?> codec = TypeDefinitionAwareCodec.fromType(def);
        assertThat(codec, instanceOf(clazz));
        return clazz.cast(codec);
    }

    public static void deserializeWithExpectedIllegalArgEx(final Codec<String, ?, IllegalArgumentException> codec,
            final @NonNull String param) {
        try {
            codec.deserialize(param);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected ...
        }
    }

    public static EnumTypeDefinition toEnumTypeDefinition(final String... enums) {
        final EnumerationTypeBuilder b = BaseTypes.enumerationTypeBuilder(QName.create("foo", "foo"));
        int val = 0;
        for (String en : enums) {
            EnumTypeDefinition.EnumPair mockEnum = mock(EnumPair.class);
            doReturn(en).when(mockEnum).getName();
            doReturn(val).when(mockEnum).getValue();
            b.addEnum(mockEnum);
            val++;
        }

        return b.build();
    }
}
