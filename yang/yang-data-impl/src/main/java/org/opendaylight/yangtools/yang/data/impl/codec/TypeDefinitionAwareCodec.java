package org.opendaylight.yangtools.yang.data.impl.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.*;

public abstract class TypeDefinitionAwareCodec<T> implements StringCodec<T> {

    private final TypeDefinition<?> typeDefinition;
    private final Class<T> inputClass;

    public Class<T> getInputClass() {
        return inputClass;
    }

    protected TypeDefinitionAwareCodec(TypeDefinition<?> typeDefinition, Class<T> outputClass) {
        this.typeDefinition = typeDefinition;
        this.inputClass = outputClass;
    }

    public TypeDefinition<?> getTypeDefinition() {
        return typeDefinition;
    }

    @Override
    public String serialize(T data) {
        return data.toString();
    }

    public static final TypeDefinitionAwareCodec<?> from(TypeDefinition<?> typeDefinition) {
        while(typeDefinition.getBaseType() != null) {
            typeDefinition = typeDefinition.getBaseType();
        }
        
        if (BINARY_QNAME.equals(typeDefinition.getQName())) {
            return new BinaryCodec(typeDefinition);
        }
        if (BITS_QNAME.equals(typeDefinition.getQName())) {
            return new BitsCodec(typeDefinition);
        }
        if (BOOLEAN_QNAME.equals(typeDefinition.getQName())) {
            return new BooleanCodec(typeDefinition);
        }
        if (DECIMAL64_QNAME.equals(typeDefinition.getQName())) {
            return new DecimalCodec(typeDefinition);
        }
        if (EMPTY_QNAME.equals(typeDefinition.getQName())) {
            return new EmptyCodec(typeDefinition);
        }
        if (ENUMERATION_QNAME.equals(typeDefinition.getQName())) {
            return new EnumCodec(typeDefinition);
        }
        if (INT8_QNAME.equals(typeDefinition.getQName())) {
            return new Int8Codec(typeDefinition);
        }
        if (INT16_QNAME.equals(typeDefinition.getQName())) {
            return new Int16Codec(typeDefinition);
        }
        if (INT32_QNAME.equals(typeDefinition.getQName())) {
            return new Int32Codec(typeDefinition);
        }
        if (INT64_QNAME.equals(typeDefinition.getQName())) {
            return new Int64Codec(typeDefinition);
        }
        if (STRING_QNAME.equals(typeDefinition.getQName())) {
            return new StringCodec(typeDefinition);
        }
        if (UINT8_QNAME.equals(typeDefinition.getQName())) {
            return new Uint8Codec(typeDefinition);
        }
        if (UINT16_QNAME.equals(typeDefinition.getQName())) {
            return new Uint16Codec(typeDefinition);
        }
        if (UINT32_QNAME.equals(typeDefinition.getQName())) {
            return new Uint32Codec(typeDefinition);
        }
        if (UINT64_QNAME.equals(typeDefinition.getQName())) {
            return new Uint64Codec(typeDefinition);
        }
        return null;
    }

    public static class BooleanCodec extends TypeDefinitionAwareCodec<Boolean> {

        protected BooleanCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Boolean.class);
        }

        @Override
        public String serialize(Boolean data) {
            return data.toString();
        }

        @Override
        public Boolean deserialize(String stringRepresentation) {
            return Boolean.parseBoolean(stringRepresentation);
        }
    };

    public static class Uint8Codec extends TypeDefinitionAwareCodec<Short> {

        protected Uint8Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Short.class);
        }

        @Override
        public String serialize(Short data) {
            return data.toString();
        }

        @Override
        public Short deserialize(String stringRepresentation) {
            return Short.parseShort(stringRepresentation);
        }
    };

    public static class Uint16Codec extends TypeDefinitionAwareCodec<Integer> {
        protected Uint16Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            return Integer.parseInt(stringRepresentation);
        }
    };

    public static class Uint32Codec extends TypeDefinitionAwareCodec<Long> {

        protected Uint32Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            return Long.parseLong(stringRepresentation);
        }
    };

    public static class Uint64Codec extends TypeDefinitionAwareCodec<BigInteger> {

        protected Uint64Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, BigInteger.class);
        }

        @Override
        public BigInteger deserialize(String stringRepresentation) {
            // FIXME: Implement codec correctly
            return BigInteger.valueOf(Long.valueOf(stringRepresentation));
        }
    };

    public static class StringCodec extends TypeDefinitionAwareCodec<String> {

        protected StringCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }
    };

    public static class Int16Codec extends TypeDefinitionAwareCodec<Short> {

        protected Int16Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Short.class);
        }

        @Override
        public Short deserialize(String stringRepresentation) {
            return Short.valueOf(stringRepresentation);
        }
    };

    public static class Int32Codec extends TypeDefinitionAwareCodec<Integer> {

        protected Int32Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            return Integer.valueOf(stringRepresentation);
        }
    };

    public static class Int64Codec extends TypeDefinitionAwareCodec<Long> {

        protected Int64Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            return Long.parseLong(stringRepresentation);
        }
    };

    public static class Int8Codec extends TypeDefinitionAwareCodec<Byte> {

        protected Int8Codec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Byte.class);
        }

        @Override
        public Byte deserialize(String stringRepresentation) {
            return Byte.parseByte(stringRepresentation);
        }
    };

    public static class EmptyCodec extends TypeDefinitionAwareCodec<Void> {

        protected EmptyCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Void.class);
        }

        @Override
        public String serialize(Void data) {
            return "";
        }

        @Override
        public Void deserialize(String stringRepresentation) {
            return null;
        }
    };

    public static final class BinaryCodec extends TypeDefinitionAwareCodec<byte[]> {

        protected BinaryCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, byte[].class);
        }

        @Override
        public String serialize(byte[] data) {
            // FIXME By YANG Spec
            return null;
        }

        @Override
        public byte[] deserialize(String stringRepresentation) {
            // FIXME By YANG Spec
            return null;
        }
    };

    public static final class BitsCodec extends TypeDefinitionAwareCodec<Set<String>> {

        @SuppressWarnings("unchecked")
        protected BitsCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, (Class<Set<String>>) ((Class<?>) Set.class));
        }

        @Override
        public String serialize(Set<String> data) {
            return Joiner.on(" ").join(data).toString();
        }

        @Override
        public Set<String> deserialize(String stringRepresentation) {
            String[] strings = stringRepresentation.split(" ");
            return ImmutableSet.copyOf(strings);
        }
    };

    public static class EnumCodec extends TypeDefinitionAwareCodec<String> {

        protected EnumCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }
    };

    public static class DecimalCodec extends TypeDefinitionAwareCodec<BigDecimal> {

        protected DecimalCodec(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, BigDecimal.class);
        }

        @Override
        public String serialize(BigDecimal data) {
            return data.toString();
        }

        @Override
        public BigDecimal deserialize(String stringRepresentation) {
            return new BigDecimal(stringRepresentation);
        }
    };
}
