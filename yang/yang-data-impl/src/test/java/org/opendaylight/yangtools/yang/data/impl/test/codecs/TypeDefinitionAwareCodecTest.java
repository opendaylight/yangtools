/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.UnionType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class TypeDefinitionAwareCodecTest {

    @SuppressWarnings("unchecked")
    <T> T getCodec( TypeDefinition<?> def, Class<T> clazz ) {
        Object codec = TypeDefinitionAwareCodec.from( def );
        assertNotNull( codec );
        assertTrue( clazz.isAssignableFrom( codec.getClass() ) );
        return (T)codec;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBitsCodecStringWithEmpty() throws Exception {
        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "foo" ), BitsCodec.class );
        String serialized = codec.serialize(ImmutableSet.<String> of());
        assertEquals("serialize", "", serialized);

        Set<String> deserialized = codec.deserialize("");
        assertNotNull(deserialized);
        assertEquals("deserialize empty", true, deserialized.isEmpty());

        Set<String> deserializedFromNull = codec.deserialize(null);
        assertNotNull(deserializedFromNull);
        assertEquals("deserialize empty", true, deserialized.isEmpty());
    }

    BitsTypeDefinition toBitsTypeDefinition( String... bits ) {
        List<BitsTypeDefinition.Bit> bitList = Lists.newArrayList();
        for( String bit: bits ) {
            BitsTypeDefinition.Bit mockBit = mock( BitsTypeDefinition.Bit.class );
            when( mockBit.getName() ).thenReturn( bit );
            bitList.add( mockBit );
        }

//        BitsTypeDefinition mockBitsDef = mock( BitsTypeDefinition.class );
//        when( mockBitsDef.getBits() ).thenReturn( bitList );
        return new BitsType( null , bitList );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBitsCodecStringWithValid() throws Exception {
        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "foo", "bar" ), BitsCodec.class );

        ImmutableSet<String> toSerialize = ImmutableSet.of("foo", "bar");

        String serialized = codec.serialize(toSerialize);
        assertNotNull(serialized);
        assertTrue(serialized.contains("foo"));
        assertTrue(serialized.contains("bar"));

        Set<String> deserialized = codec.deserialize("  foo bar     ");
        assertEquals("deserialize", toSerialize, deserialized);
    }

    @SuppressWarnings("unchecked")
    @Test(expected=IllegalArgumentException.class)
    public void testBitsCodecStringWithInvalid() throws Exception {
        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "bit1", "bit2" ), BitsCodec.class );

        codec.deserialize("bit1 bit3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBitsCodecStringNull() {
        BitsCodec<String> codec = getCodec( toBitsTypeDefinition( "foo" ), BitsCodec.class );
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumCodecStringWithValid() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition( "enum1", "enum2" ), EnumCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "enum2", codec.deserialize( "enum2" ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumCodecStringWithInvalid() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition( "enum1", "enum2" ), EnumCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
    }

    EnumTypeDefinition toEnumTypeDefinition( String... enums ) {
        List<EnumTypeDefinition.EnumPair> enumList = Lists.newArrayList();
        for( String en: enums ) {
            EnumTypeDefinition.EnumPair mockEnum = mock( EnumTypeDefinition.EnumPair.class );
            when( mockEnum.getName() ).thenReturn( en );
            enumList.add( mockEnum );
        }

        return new EnumerationType( null, enumList );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnionCodecStringWithValid() {
        UnionCodec<String> codec =
            getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                                             toUnionTypeDefinition( Int32.getInstance(),
                                                                    Int64.getInstance() ) ),
                      UnionCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "123", codec.serialize( "123" ) );

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "123", codec.deserialize( "123" ) );
        assertEquals( "deserialize", "-123", codec.deserialize( "-123" ) );
        assertEquals( "deserialize", "41234567890", codec.deserialize( "41234567890" ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnionCodecStringWithInvalid() {
        UnionCodec<String> codec =
                getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                                                 toUnionTypeDefinition( Int32.getInstance(),
                                                                        Int64.getInstance() ) ),
                          UnionCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
        deserializeWithExpectedIllegalArgEx( codec, "123o" );
        deserializeWithExpectedIllegalArgEx( codec, "true" );
    }

    void deserializeWithExpectedIllegalArgEx( Codec<String,String> codec, String param ) {
        try {
            codec.deserialize( param );
            fail( "Expected IllegalArgumentException" );
        } catch (Exception e) {
            // Expected ...
        }
    }

    UnionTypeDefinition toUnionTypeDefinition( TypeDefinition<?>... types ) {
        return new UnionType( Lists.newArrayList( types ) );
    }
}
