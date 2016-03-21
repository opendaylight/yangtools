/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.Test;

import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toBitsTypeDefinition;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;

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

        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "foo" ), BitsCodec.class );

        ImmutableSet<String> toSerialize = ImmutableSet.of("foo", "bar");

        assertEquals("serialize", "bar foo", codec.serialize(toSerialize));

        assertEquals("serialize", "",
                codec.serialize(ImmutableSet.<String> of()));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {

        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "bit1", "bit2" ), BitsCodec.class );

        assertEquals("deserialize", ImmutableSet.of("bit1", "bit2"), codec.deserialize("  bit1 bit2     "));

        assertEquals("deserialize", Collections.emptySet(), codec.deserialize(""));
        assertEquals("deserialize", Collections.emptySet(), codec.deserialize(null));

        deserializeWithExpectedIllegalArgEx(codec, "bit1 bit3");
    }
}
