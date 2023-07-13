/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.UnionTypeBuilder;

/**
 * Unit tests forUnionCodecString.
 *
 * @author Thomas Pantelis
 */
public class UnionCodecStringTest {
    private static UnionTypeDefinition toUnionTypeDefinition(final TypeDefinition<?>... types) {
        final UnionTypeBuilder builder = BaseTypes.unionTypeBuilder(QName.create("foo", "foo"));

        for (TypeDefinition<?> t : types) {
            builder.addType(t);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        UnionCodec<String> codec = getCodec(toUnionTypeDefinition(toEnumTypeDefinition("enum1", "enum2"),
                toUnionTypeDefinition(BaseTypes.int32Type(),BaseTypes.int64Type()), BaseTypes.emptyType()),
                UnionCodec.class);

        assertEquals("enum1", codec.serialize("enum1"), "serialize");
        assertEquals("123", codec.serialize("123"), "serialize");
        assertEquals("123", codec.serialize(123), "serialize");
        assertEquals("", codec.serialize(""), "serialize");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        UnionCodec<String> codec = getCodec(toUnionTypeDefinition(toEnumTypeDefinition("enum1", "enum2"),
                toUnionTypeDefinition(BaseTypes.int32Type(),BaseTypes.int64Type()), BaseTypes.emptyType()),
                UnionCodec.class);

        assertEquals("enum1", codec.deserialize("enum1"), "deserialize");
        assertEquals(123, codec.deserialize("123"), "deserialize");
        assertEquals(-123, codec.deserialize("-123"), "deserialize");
        assertEquals(41234567890L, codec.deserialize("41234567890"), "deserialize");
        assertEquals(Empty.value(), codec.deserialize(""), "deserialize");

        deserializeWithExpectedIllegalArgEx(codec, "enum3");
        deserializeWithExpectedIllegalArgEx(codec, "123o");
        deserializeWithExpectedIllegalArgEx(codec, "true");
    }
}
