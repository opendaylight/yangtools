/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;

/**
 * Unit tests for EnumCodecString.
 *
 * @author Thomas Pantelis
 */
public class EnumCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition( "enum1", "enum2" ), EnumCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "", codec.serialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition( "enum1", "enum2" ), EnumCodec.class);

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "enum2", codec.deserialize( "enum2" ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
    }
}
