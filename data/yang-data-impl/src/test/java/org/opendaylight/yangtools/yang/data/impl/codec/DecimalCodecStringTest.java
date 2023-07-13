/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for DecimalCodecString.
 *
 * @author Thomas Pantelis
 */
public class DecimalCodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        DecimalCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(getType(), DecimalCodec.class);
        assertEquals("123.456", codec.serialize(Decimal64.valueOf("123.456")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        DecimalCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(getType(), DecimalCodec.class);

        assertEquals(Decimal64.valueOf("123.456"), codec.deserialize("123.456"));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "12o.3");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }

    private static DecimalTypeDefinition getType() {
        return BaseTypes.decimalTypeBuilder(QName.create("foo", "foo")).setFractionDigits(3).build();
    }
}
