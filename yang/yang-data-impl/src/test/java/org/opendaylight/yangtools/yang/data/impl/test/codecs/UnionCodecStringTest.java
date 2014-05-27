/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toUnionTypeDefinition;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;

/**
 * Unit tests forUnionCodecString.
 *
 * @author Thomas Pantelis
 */
public class UnionCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        UnionCodec<String> codec =
            getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                                             toUnionTypeDefinition( Int32.getInstance(),
                                                                    Int64.getInstance() ) ),
                      UnionCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "123", codec.serialize( "123" ) );
        assertEquals( "serialize", "", codec.serialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        UnionCodec<String> codec =
            getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                                             toUnionTypeDefinition( Int32.getInstance(),
                                                                    Int64.getInstance() ) ),
                      UnionCodec.class);

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", Integer.valueOf( 123 ), codec.deserialize( "123" ) );
        assertEquals( "deserialize", Integer.valueOf( -123 ), codec.deserialize( "-123" ) );
        assertEquals( "deserialize", Long.valueOf( 41234567890L ), codec.deserialize( "41234567890" ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
        deserializeWithExpectedIllegalArgEx( codec, "123o" );
        deserializeWithExpectedIllegalArgEx( codec, "true" );
    }
}
