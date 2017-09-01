/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for DecimalCodecString.
 *
 * @author Thomas Pantelis
 */
public class DecimalCodecStringTest {

    private static DecimalTypeDefinition getType() {
        return BaseTypes.decimalTypeBuilder(mock(SchemaPath.class)).setFractionDigits(2).build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        DecimalCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(getType(), DecimalCodec.class);

        assertEquals("serialize", "123.456", codec.serialize(new BigDecimal("123.456")));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        DecimalCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(getType(), DecimalCodec.class);

        assertEquals("deserialize", new BigDecimal("123.456"), codec.deserialize("123.456"));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "12o.3");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
