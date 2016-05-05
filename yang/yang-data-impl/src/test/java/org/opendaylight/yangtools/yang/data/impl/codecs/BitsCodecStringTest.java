/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.Test;

import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for BitsCodecString.
 *
 * @author Thomas Pantelis
 *
 */
public class BitsCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {

        BitsCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec( TypeDefinitionAwareCodecTestHelper.toBitsTypeDefinition( "foo" ), BitsCodec.class );

        ImmutableSet<String> toSerialize = ImmutableSet.of("foo", "bar");

        String serialized = codec.serialize(toSerialize);
        assertNotNull(serialized);
        assertTrue(serialized.contains("foo"));
        assertTrue(serialized.contains("bar"));

        assertEquals("serialize", "",
                codec.serialize(ImmutableSet.of()));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {

        BitsCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec( TypeDefinitionAwareCodecTestHelper.toBitsTypeDefinition( "bit1", "bit2" ), BitsCodec.class );

        assertEquals("deserialize", ImmutableSet.of("bit1", "bit2"), codec.deserialize("  bit1 bit2     "));

        assertEquals("deserialize", Collections.emptySet(), codec.deserialize(""));
        assertEquals("deserialize", Collections.emptySet(), codec.deserialize(null));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit1 bit3");
    }
}
