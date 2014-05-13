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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BinaryType;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.BooleanType;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.EmptyType;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.UnionType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

public class TypeDefinitionAwareCodecTest {

    @SuppressWarnings("unchecked")
    static <T> T getCodec( TypeDefinition<?> def, Class<T> clazz ) {
        Object codec = TypeDefinitionAwareCodec.from( def );
        assertNotNull( codec );
        assertTrue( clazz.isAssignableFrom( codec.getClass() ) );
        return (T)codec;
    }

    static void deserializeWithExpectedIllegalArgEx( Codec<String,?> codec, String param ) {
        try {
            codec.deserialize( param );
            fail( "Expected IllegalArgumentException" );
        } catch (Exception e) {
            // Expected ...
        }
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
    public void testEnumCodecString() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition( "enum1", "enum2" ), EnumCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "", codec.serialize( null ) );

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "enum2", codec.deserialize( "enum2" ) );

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
    public void testUnionCodecString() {
        UnionCodec<String> codec =
            getCodec( toUnionTypeDefinition( toEnumTypeDefinition( "enum1", "enum2" ),
                                             toUnionTypeDefinition( Int32.getInstance(),
                                                                    Int64.getInstance() ) ),
                      UnionCodec.class);

        assertEquals( "serialize", "enum1", codec.serialize( "enum1" ) );
        assertEquals( "serialize", "123", codec.serialize( "123" ) );
        assertEquals( "serialize", "", codec.serialize( null ) );

        assertEquals( "deserialize", "enum1", codec.deserialize( "enum1" ) );
        assertEquals( "deserialize", "123", codec.deserialize( "123" ) );
        assertEquals( "deserialize", "-123", codec.deserialize( "-123" ) );
        assertEquals( "deserialize", "41234567890", codec.deserialize( "41234567890" ) );

        deserializeWithExpectedIllegalArgEx( codec, "enum3" );
        deserializeWithExpectedIllegalArgEx( codec, "123o" );
        deserializeWithExpectedIllegalArgEx( codec, "true" );
    }

    UnionTypeDefinition toUnionTypeDefinition( TypeDefinition<?>... types ) {
        return new UnionType( Lists.newArrayList( types ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyCodecString() {
        EmptyCodec<String> codec = getCodec( EmptyType.getInstance(), EmptyCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );
        assertEquals( "deserialize", null, codec.deserialize( "" ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );

        deserializeWithExpectedIllegalArgEx( codec, "foo" );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBooleanCodecString() {
        BooleanCodec<String> codec = getCodec( BooleanType.getInstance(), BooleanCodec.class);

        assertEquals( "serialize", "", codec.serialize( null ) );
        assertEquals( "serialize", "true", codec.serialize( true ) );
        assertEquals( "serialize", "false", codec.serialize( false ) );

        assertEquals( "deserialize", Boolean.TRUE, codec.deserialize( "true" ) );
        assertEquals( "deserialize", Boolean.TRUE, codec.deserialize( "TRUE" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "FALSE" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "false" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "foo" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( "" ) );
        assertEquals( "deserialize", Boolean.FALSE, codec.deserialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStringCodecString() {
        StringCodec<String> codec = getCodec( StringType.getInstance(), StringCodec.class);

        assertEquals( "serialize", "foo", codec.serialize( "foo" ) );
        assertEquals( "serialize", "", codec.serialize( "" ) );
        assertEquals( "serialize", "", codec.serialize( null ) );

        assertEquals( "deserialize", "bar", codec.deserialize( "bar" ) );
        assertEquals( "deserialize", "", codec.deserialize( "" ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBinaryCodecString() {
        BinaryCodec<String> codec = getCodec( BinaryType.getInstance(), BinaryCodec.class);

        assertEquals( "serialize", BaseEncoding.base64().encode( new byte[]{1,2,3,4} ),
                      codec.serialize( new byte[]{1,2,3,4} ) );
        assertEquals( "serialize", "", codec.serialize( null ) );

        assertArrayEquals( "deserialize", new byte[]{1,2,3,4},
                      codec.deserialize( BaseEncoding.base64().encode( new byte[]{1,2,3,4} ) ) );
        assertEquals( "deserialize", null, codec.deserialize( null ) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecimalCodecString() {
        DecimalCodec<String> codec = getCodec( new Decimal64(null, 2), DecimalCodec.class);

        assertEquals( "serialize", "123.456", codec.serialize( new BigDecimal( "123.456" ) ) );
        assertEquals( "serialize", "", codec.serialize( null ) );

        assertEquals( "deserialize", new BigDecimal( "123.456" ), codec.deserialize( "123.456" ) );

        deserializeWithExpectedIllegalArgEx( codec, "12o.3" );
        deserializeWithExpectedIllegalArgEx( codec, "" );
        deserializeWithExpectedIllegalArgEx( codec, null );
    }
}
