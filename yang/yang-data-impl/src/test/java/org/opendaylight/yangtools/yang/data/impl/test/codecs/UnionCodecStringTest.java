/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;
import static org.opendaylight.yangtools.yang.data.impl.test.codecs.TypeDefinitionAwareCodecTestHelper.toUnionTypeDefinition;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

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
                toUnionTypeDefinition( BaseTypes.int32Type(), BaseTypes.int64Type()), BaseTypes.emptyType() ),
                      UnionCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "123", codec.serialize( "123" ) );
        assertEquals( "serialize", "123", codec.serialize( 123 ) );
        assertEquals( "serialize", "", codec.serialize( null ) );
        assertEquals( "serialize", "", codec.serialize( "" ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        UnionCodec<String> codec =
            getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                toUnionTypeDefinition( BaseTypes.int32Type(), BaseTypes.int64Type()), BaseTypes.emptyType() ),
                      UnionCodec.class);

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "123", codec.deserialize( "123" ) );
        assertEquals( "deserialize", "-123", codec.deserialize( "-123" ) );
        assertEquals( "deserialize", "41234567890", codec.deserialize( "41234567890" ) );
        assertEquals( "deserialize", "", codec.deserialize( "" ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
        deserializeWithExpectedIllegalArgEx( codec, "123o" );
        deserializeWithExpectedIllegalArgEx( codec, "true" );
    }
}
