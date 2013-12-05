package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.BINARY_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.BITS_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.BOOLEAN_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.DECIMAL64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.EMPTY_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.ENUMERATION_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT8_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.STRING_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT8_QNAME;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.data.api.codec.Int16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public abstract class TypeDefinitionAwareCodec<T> implements DataStringCodec<T> {

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

    public static final TypeDefinitionAwareCodec<?> from(TypeDefinition<?> typeDefinition) {
        while(typeDefinition.getBaseType() != null) {
            typeDefinition = typeDefinition.getBaseType();
        }
        
        if (BINARY_QNAME.equals(typeDefinition.getQName())) {
            return new BinaryCodecStringImpl(typeDefinition);
        }
        if (BITS_QNAME.equals(typeDefinition.getQName())) {
            return new BitsCodecStringImpl(typeDefinition);
        }
        if (BOOLEAN_QNAME.equals(typeDefinition.getQName())) {
            return new BooleanCodecStringImpl(typeDefinition);
        }
        if (DECIMAL64_QNAME.equals(typeDefinition.getQName())) {
            return new DecimalCodecStringImpl(typeDefinition);
        }
        if (EMPTY_QNAME.equals(typeDefinition.getQName())) {
            return new EmptyCodecStringImpl(typeDefinition);
        }
        if (ENUMERATION_QNAME.equals(typeDefinition.getQName())) {
            return new EnumCodecStringImpl(typeDefinition);
        }
        if (INT8_QNAME.equals(typeDefinition.getQName())) {
            return new Int8CodecStringImpl(typeDefinition);
        }
        if (INT16_QNAME.equals(typeDefinition.getQName())) {
            return new Int16CodecStringImpl(typeDefinition);
        }
        if (INT32_QNAME.equals(typeDefinition.getQName())) {
            return new Int32CodecStringImpl(typeDefinition);
        }
        if (INT64_QNAME.equals(typeDefinition.getQName())) {
            return new Int64CodecStringImpl(typeDefinition);
        }
        if (STRING_QNAME.equals(typeDefinition.getQName())) {
            return new StringCodecStringImpl(typeDefinition);
        }
        if (UINT8_QNAME.equals(typeDefinition.getQName())) {
            return new Uint8CodecStringImpl(typeDefinition);
        }
        if (UINT16_QNAME.equals(typeDefinition.getQName())) {
            return new Uint16CodecStringImpl(typeDefinition);
        }
        if (UINT32_QNAME.equals(typeDefinition.getQName())) {
            return new Uint32CodecStringImpl(typeDefinition);
        }
        if (UINT64_QNAME.equals(typeDefinition.getQName())) {
            return new Uint64CodecStringImpl(typeDefinition);
        }
        return null;
    }

    public static class BooleanCodecStringImpl extends TypeDefinitionAwareCodec<Boolean> implements BooleanCodec<String> {

        protected BooleanCodecStringImpl(TypeDefinition<?> typeDefinition) {
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

    public static class Uint8CodecStringImpl extends TypeDefinitionAwareCodec<Short> implements Uint8Codec<String> {

        protected Uint8CodecStringImpl(TypeDefinition<?> typeDefinition) {
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

    public static class Uint16CodecStringImpl extends TypeDefinitionAwareCodec<Integer> implements Uint16Codec<String> {
        protected Uint16CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            return Integer.parseInt(stringRepresentation);
        }

        @Override
        public String serialize(Integer data) {
            return data.toString();
        }
    };

    public static class Uint32CodecStringImpl extends TypeDefinitionAwareCodec<Long> implements Uint32Codec<String> {

        protected Uint32CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            return Long.parseLong(stringRepresentation);
        }

        @Override
        public String serialize(Long data) {
            return data.toString();
        }
    };

    public static class Uint64CodecStringImpl extends TypeDefinitionAwareCodec<BigInteger> implements Uint64Codec<String> {

        protected Uint64CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, BigInteger.class);
        }

        @Override
        public BigInteger deserialize(String stringRepresentation) {
            // FIXME: Implement codec correctly
            return BigInteger.valueOf(Long.valueOf(stringRepresentation));
        }

        @Override
        public String serialize(BigInteger data) {
            return data.toString();
        }
    };

    public static class StringCodecStringImpl extends TypeDefinitionAwareCodec<String> implements StringCodec<String> {

        protected StringCodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }

        @Override
        public String serialize(String data) {
            return data.toString();
        }
    };

    public static class Int16CodecStringImpl extends TypeDefinitionAwareCodec<Short> implements Int16Codec<String> {

        protected Int16CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Short.class);
        }

        @Override
        public Short deserialize(String stringRepresentation) {
            return Short.valueOf(stringRepresentation);
        }

        @Override
        public String serialize(Short data) {
            return data.toString();
        }
    };

    public static class Int32CodecStringImpl extends TypeDefinitionAwareCodec<Integer> implements Int32Codec<String> {

        protected Int32CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Integer.class);
        }

        @Override
        public Integer deserialize(String stringRepresentation) {
            return Integer.valueOf(stringRepresentation);
        }

        @Override
        public String serialize(Integer data) {
            return data.toString();
        }
    };

    public static class Int64CodecStringImpl extends TypeDefinitionAwareCodec<Long> implements Int64Codec<String> {

        protected Int64CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Long.class);
        }

        @Override
        public Long deserialize(String stringRepresentation) {
            return Long.parseLong(stringRepresentation);
        }

        @Override
        public String serialize(Long data) {
            return data.toString();
        }
    };

    public static class Int8CodecStringImpl extends TypeDefinitionAwareCodec<Byte> implements Int8Codec<String> {

        protected Int8CodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, Byte.class);
        }

        @Override
        public Byte deserialize(String stringRepresentation) {
            return Byte.parseByte(stringRepresentation);
        }

        @Override
        public String serialize(Byte data) {
            return data.toString();
        }
    };

    public static class EmptyCodecStringImpl extends TypeDefinitionAwareCodec<Void> implements EmptyCodec<String> {

        protected EmptyCodecStringImpl(TypeDefinition<?> typeDefinition) {
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

    public static final class BinaryCodecStringImpl extends TypeDefinitionAwareCodec<byte[]> implements BinaryCodec<String> {

        protected BinaryCodecStringImpl(TypeDefinition<?> typeDefinition) {
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

    public static final class BitsCodecStringImpl extends TypeDefinitionAwareCodec<Set<String>> implements BitsCodec<String> {

        @SuppressWarnings("unchecked")
        protected BitsCodecStringImpl(TypeDefinition<?> typeDefinition) {
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

    public static class EnumCodecStringImpl extends TypeDefinitionAwareCodec<String> implements EnumCodec<String> {

        protected EnumCodecStringImpl(TypeDefinition<?> typeDefinition) {
            super(typeDefinition, String.class);
        }

        @Override
        public String deserialize(String stringRepresentation) {
            return stringRepresentation;
        }

        @Override
        public String serialize(String data) {
            return data.toString();
        }
    };

    public static class DecimalCodecStringImpl extends TypeDefinitionAwareCodec<BigDecimal> implements DecimalCodec<String> {

        protected DecimalCodecStringImpl(TypeDefinition<?> typeDefinition) {
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
