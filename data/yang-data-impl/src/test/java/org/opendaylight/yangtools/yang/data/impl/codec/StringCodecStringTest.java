/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.ri.type.StringTypeBuilder;

/**
 * Unit tests for StringCodecString.
 *
 * @author Thomas Pantelis
 */
public class StringCodecStringTest {
    @Test
    public void testSerialize() {
        StringCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("foo", codec.serialize("foo"), "serialize");
        assertEquals("", codec.serialize(""), "serialize");
    }

    @Test
    public void testDeserialize() {
        StringCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("bar", codec.deserialize("bar"), "deserialize");
        assertEquals("", codec.deserialize(""), "deserialize");
    }

    @Test
    public void testDeserializeUnicode() throws InvalidLengthConstraintException {
        final StringTypeBuilder builder = RestrictedTypes.newStringBuilder(BaseTypes.stringType(),
            QName.create("foo", "foo"));
        builder.setLengthConstraint(mock(ConstraintMetaDefinition.class), List.of(ValueRange.of(1)));
        final StringTypeDefinition type = builder.build();

        StringCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(type, StringCodec.class);

        assertEquals("🌞", codec.deserialize("🌞"));
    }
}
