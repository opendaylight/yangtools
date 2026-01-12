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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.ri.type.RestrictedTypes;

/**
 * Unit tests for StringCodecString.
 *
 * @author Thomas Pantelis
 */
class StringCodecStringTest {
    @Test
    void testSerialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("foo", codec.serialize("foo"), "serialize");
        assertEquals("", codec.serialize(""), "serialize");
    }

    @Test
    void testDeserialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.stringType(),
            StringCodec.class);

        assertEquals("bar", codec.deserialize("bar"), "deserialize");
        assertEquals("", codec.deserialize(""), "deserialize");
    }

    @Test
    void testDeserializeUnicode() throws InvalidLengthConstraintException {
        final var builder = RestrictedTypes.newStringBuilder(BaseTypes.stringType(),
            QName.create("foo", "foo"));
        builder.setLengthConstraint(mock(ConstraintMetaDefinition.class), ValueRanges.of(ValueRange.of(1)));
        final var type = builder.build();

        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(type, StringCodec.class);

        assertEquals("🌞", codec.deserialize("🌞"));
    }
}
