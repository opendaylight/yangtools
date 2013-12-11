package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.opendaylight.yangtools.yang.data.api.codec.*;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.*;

import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {

    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;

    public static final BinaryCodecStringImpl BINARY_DEFAULT_CODEC = new BinaryCodecStringImpl(
            Optional.<BinaryTypeDefinition> absent());

    public static final BitsCodecStringImpl BITS_DEFAULT_CODEC = new BitsCodecStringImpl(
            Optional.<BitsTypeDefinition> absent());

    public static final BooleanCodecStringImpl BOOLEAN_DEFAULT_CODEC = new BooleanCodecStringImpl(
            Optional.<BooleanTypeDefinition> absent());

    public static final DecimalCodecStringImpl DECIMAL64_DEFAULT_CODEC = new DecimalCodecStringImpl(
            Optional.<DecimalTypeDefinition> absent());

    public static final EmptyCodecStringImpl EMPTY_DEFAULT_CODEC = new EmptyCodecStringImpl(
            Optional.<EmptyTypeDefinition> absent());

    public static final EnumCodecStringImpl ENUMERATION_DEFAULT_CODEC = new EnumCodecStringImpl(
            Optional.<EnumTypeDefinition> absent());

    public static final Int8CodecStringImpl INT8_DEFAULT_CODEC = new Int8CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int16CodecStringImpl INT16_DEFAULT_CODEC = new Int16CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int32CodecStringImpl INT32_DEFAULT_CODEC = new Int32CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final Int64CodecStringImpl INT64_DEFAULT_CODEC = new Int64CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    public static final StringCodecStringImpl STRING_DEFAULT_CODEC = new StringCodecStringImpl(
            Optional.<StringTypeDefinition> absent());

    public static final Uint8CodecStringImpl UINT8_DEFAULT_CODEC = new Uint8CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint16CodecStringImpl UINT16_DEFAULT_CODEC = new Uint16CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint32CodecStringImpl UINT32_DEFAULT_CODEC = new Uint32CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public static final Uint64CodecStringImpl UINT64_DEFAULT_CODEC = new Uint64CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    public Class<J> getInputClass() {
        return inputClass;
    }

    protected TypeDefinitionAwareCodec(Optional<T> typeDefinition, Class<J> outputClass) {
        Preconditions.checkArgument(outputClass != null, "Output class must be specified.");
        this.typeDefinition = typeDefinition;
        this.inputClass = outputClass;
    }

    public Optional<T> getTypeDefinition() {
        return typeDefinition;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(TypeDefinition typeDefinition) {
        final TypeDefinitionAwareCodec codec = fromType(typeDefinition);
        return (TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>>) codec;
    }

    public static final <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(T typeDefinition) {
        T superType = typeDefinition;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }

        @SuppressWarnings("rawtypes")
        TypeDefinitionAwareCodec codec = null;

        if (superType instanceof BinaryTypeDefinition) {
            codec = BINARY_DEFAULT_CODEC;
        } else if (superType instanceof BitsTypeDefinition) {
            codec = BITS_DEFAULT_CODEC;
        } else if (superType instanceof BooleanTypeDefinition) {
            codec = BOOLEAN_DEFAULT_CODEC;
        } else if (superType instanceof DecimalTypeDefinition) {
            codec = DECIMAL64_DEFAULT_CODEC;
        } else if (superType instanceof EmptyTypeDefinition) {
            codec = EMPTY_DEFAULT_CODEC;
        } else if (superType instanceof EnumTypeDefinition) {
            codec = ENUMERATION_DEFAULT_CODEC;
        } else if (superType instanceof IntegerTypeDefinition) {
            if (INT8_QNAME.equals(superType.getQName())) {
                codec = INT8_DEFAULT_CODEC;
            } else if (INT16_QNAME.equals(superType.getQName())) {
                codec = INT16_DEFAULT_CODEC;
            } else if (INT32_QNAME.equals(superType.getQName())) {
                codec = INT32_DEFAULT_CODEC;
            } else if (INT64_QNAME.equals(superType.getQName())) {
                codec = INT64_DEFAULT_CODEC;
            }
        } else if (superType instanceof StringTypeDefinition) {
            codec = STRING_DEFAULT_CODEC;
        } else if (superType instanceof UnsignedIntegerTypeDefinition) {
            if (UINT8_QNAME.equals(superType.getQName())) {
                codec = UINT8_DEFAULT_CODEC;
            }
            if (UINT16_QNAME.equals(superType.getQName())) {
                codec = UINT16_DEFAULT_CODEC;
            }
            if (UINT32_QNAME.equals(superType.getQName())) {
                codec = UINT32_DEFAULT_CODEC;
            }
            if (UINT64_QNAME.equals(superType.getQName())) {
                codec = UINT64_DEFAULT_CODEC;
            }
        }
        return (TypeDefinitionAwareCodec<?, T>) codec;
    }

    public static class BooleanCodecStringImpl extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
            implements BooleanCodec<String> {

        protected BooleanCodecStringImpl(Optional<BooleanTypeDefinition> typeDef) {
            super(typeDef, Boolean.class);
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

    public static class Uint8CodecStringImpl extends TypeDefinitionAwareCodec<Short, UnsignedIntegerTypeDefinition>
            implements Uint8Codec<String> {

        protected Uint8CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
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

    public static class Uint16CodecStringImpl extends TypeDefinitionAwareCodec<Integer, UnsignedIntegerTypeDefinition>
            implements Uint16Codec<String> {
        protected Uint16CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
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

    public static class Uint32CodecStringImpl extends TypeDefinitionAwareCodec<Long, UnsignedIntegerTypeDefinition>
            implements Uint32Codec<String> {

        protected Uint32CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
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

    public static class Uint64CodecStringImpl extends
            TypeDefinitionAwareCodec<BigInteger, UnsignedIntegerTypeDefinition> implements Uint64Codec<String> {

        protected Uint64CodecStringImpl(Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, BigInteger.class);
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

    public static class StringCodecStringImpl extends TypeDefinitionAwareCodec<String, StringTypeDefinition> implements
            StringCodec<String> {

        protected StringCodecStringImpl(Optional<StringTypeDefinition> typeDef) {
            super(typeDef, String.class);
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

    public static class Int16CodecStringImpl extends TypeDefinitionAwareCodec<Short, IntegerTypeDefinition> implements
            Int16Codec<String> {

        protected Int16CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
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

    public static class Int32CodecStringImpl extends TypeDefinitionAwareCodec<Integer, IntegerTypeDefinition> implements
            Int32Codec<String> {

        protected Int32CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
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

    public static class Int64CodecStringImpl extends TypeDefinitionAwareCodec<Long, IntegerTypeDefinition> implements
            Int64Codec<String> {

        protected Int64CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
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

    public static class Int8CodecStringImpl extends TypeDefinitionAwareCodec<Byte, IntegerTypeDefinition> implements
            Int8Codec<String> {

        protected Int8CodecStringImpl(Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Byte.class);
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

    public static class EmptyCodecStringImpl extends TypeDefinitionAwareCodec<Void, EmptyTypeDefinition> implements
            EmptyCodec<String> {

        protected EmptyCodecStringImpl(Optional<EmptyTypeDefinition> typeDef) {
            super(typeDef, Void.class);
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

    public static final class BinaryCodecStringImpl extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
            implements BinaryCodec<String> {

        protected BinaryCodecStringImpl(Optional<BinaryTypeDefinition> typeDef) {
            super(typeDef, byte[].class);
        }

        @Override
        public String serialize(byte[] data) {
            return BaseEncoding.base64().encode(data);
        }

        @Override
        public byte[] deserialize(String stringRepresentation) {
            return BaseEncoding.base64().decode(stringRepresentation);
        }
    };

    public static final class BitsCodecStringImpl extends TypeDefinitionAwareCodec<Set<String>, BitsTypeDefinition>
            implements BitsCodec<String> {

        @SuppressWarnings("unchecked")
        protected BitsCodecStringImpl(Optional<BitsTypeDefinition> typeDef) {
            super(typeDef, (Class<Set<String>>) ((Class<?>) Set.class));
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

    public static class EnumCodecStringImpl extends TypeDefinitionAwareCodec<String, EnumTypeDefinition> implements
            EnumCodec<String> {

        protected EnumCodecStringImpl(Optional<EnumTypeDefinition> typeDef) {
            super(typeDef, String.class);
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

    public static class DecimalCodecStringImpl extends TypeDefinitionAwareCodec<BigDecimal, DecimalTypeDefinition>
            implements DecimalCodec<String> {

        protected DecimalCodecStringImpl(Optional<DecimalTypeDefinition> typeDef) {
            super(typeDef, BigDecimal.class);
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
