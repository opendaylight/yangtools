/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;
import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests forUnionCodecString.
 *
 * @author Thomas Pantelis
 */
public class UnionCodecStringTest {
    private static UnionTypeDefinition toUnionTypeDefinition(final TypeDefinition<?>... types) {
        return UnionType.create(Arrays.asList(types));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        UnionCodec<String> codec = getCodec(toUnionTypeDefinition(toEnumTypeDefinition("enum1", "enum2"),
                toUnionTypeDefinition(BaseTypes.int32Type(),BaseTypes.int64Type()), BaseTypes.emptyType()),
                UnionCodec.class);

        assertEquals("serialize", "enum1", codec.serialize("enum1"));
        assertEquals("serialize", "123", codec.serialize("123"));
        assertEquals("serialize", "123", codec.serialize(123));
        assertEquals("serialize", "", codec.serialize(null));
        assertEquals("serialize", "", codec.serialize("" ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        UnionCodec<String> codec = getCodec(toUnionTypeDefinition(toEnumTypeDefinition("enum1", "enum2"),
                toUnionTypeDefinition(BaseTypes.int32Type(),BaseTypes.int64Type()), BaseTypes.emptyType()),
                UnionCodec.class);

        assertEquals("deserialize", "enum1", codec.deserialize("enum1"));
        assertEquals("deserialize", Integer.valueOf(123), codec.deserialize("123"));
        assertEquals("deserialize", Integer.valueOf(-123), codec.deserialize("-123"));
        assertEquals("deserialize", Long.valueOf(41234567890L), codec.deserialize("41234567890"));
        assertEquals("deserialize", "", codec.deserialize(""));
        assertEquals("deserialize", null, codec.deserialize(null));

        deserializeWithExpectedIllegalArgEx(codec, "enum3");
        deserializeWithExpectedIllegalArgEx(codec, "123o");
        deserializeWithExpectedIllegalArgEx(codec, "true");
    }
}
