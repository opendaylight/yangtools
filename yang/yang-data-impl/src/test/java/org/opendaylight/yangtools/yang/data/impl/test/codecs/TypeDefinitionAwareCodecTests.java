package org.opendaylight.yangtools.yang.data.impl.test.codecs;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;

import com.google.common.collect.ImmutableSet;

public class TypeDefinitionAwareCodecTests {

    @Test
    public void bitsEmptySerialization() throws Exception {
        String serialized = TypeDefinitionAwareCodec.BITS_DEFAULT_CODEC.serialize(ImmutableSet.<String> of());
        assertNotNull(serialized);
        assertEquals("", serialized);

        Set<String> deserialized = TypeDefinitionAwareCodec.BITS_DEFAULT_CODEC.deserialize("");
        assertNotNull(deserialized);
        assertTrue(deserialized.isEmpty());

        Set<String> deserializedFromNull = TypeDefinitionAwareCodec.BITS_DEFAULT_CODEC.deserialize(null);
        assertNotNull(deserializedFromNull);
        assertTrue(deserializedFromNull.isEmpty());
    }

    @Test
    public void bitsMultipleSerialization() throws Exception {
        ImmutableSet<String> toSerialize = ImmutableSet.of("foo", "bar");

        String serialized = TypeDefinitionAwareCodec.BITS_DEFAULT_CODEC.serialize(toSerialize);
        assertNotNull(serialized);
        assertTrue(serialized.contains("foo"));
        assertTrue(serialized.contains("bar"));

        Set<String> deserialized = TypeDefinitionAwareCodec.BITS_DEFAULT_CODEC.deserialize("  foo bar     ");
        assertNotNull(deserialized);
        assertEquals(toSerialize, deserialized);
    }

}
