/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT8_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT8_QNAME;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
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
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {

    private static final Pattern intPattern = Pattern.compile("[+-]?[1-9][0-9]*$");
    private static final Pattern hexPattern = Pattern.compile("[+-]?0[xX][0-9a-fA-F]+");
    private static final Pattern octalPattern = Pattern.compile("[+-]?0[1-7][0-7]*$");

    // For up to two characters, this is very fast
    private static final CharMatcher X_MATCHER = CharMatcher.anyOf("xX");

    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;

    private static final int provideBase(final String integer) {
        if (integer == null) {
            throw new IllegalArgumentException("String representing integer number cannot be NULL");
        }

        if ((integer.length() == 1) && (integer.charAt(0) == '0')) {
            return 10;
        }

        final Matcher intMatcher = intPattern.matcher(integer);
        if (intMatcher.matches()) {
            return 10;
        } else {
            final Matcher hexMatcher = hexPattern.matcher(integer);
            if (hexMatcher.matches()) {
                return 16;
            } else {
                final Matcher octMatcher = octalPattern.matcher(integer);
                if (octMatcher.matches()) {
                    return 8;
                } else {
                    String formatedMessage = String.format("Incorrect lexical representation of integer value: %s."
                            + "%nAn integer value can be defined as: "
                            + "%n  - a decimal number,"
                            + "%n  - a hexadecimal number (prefix 0x),"
                            + "%n  - an octal number (prefix 0)."
                            + "%nSigned values are allowed. Spaces between digits are NOT allowed.", integer);
                    throw new NumberFormatException(formatedMessage);
                }
            }
        }
    }

    private static String normalizeHexadecimal(final String hexInt) {
        if (hexInt == null) {
            throw new IllegalArgumentException(
                    "String representing integer number in Hexadecimal format cannot be NULL!");
        }

        return X_MATCHER.removeFrom(hexInt);
    }

    private static final BinaryCodecStringImpl BINARY_DEFAULT_CODEC = new BinaryCodecStringImpl(
            Optional.<BinaryTypeDefinition> absent());

    private static final BooleanCodecStringImpl BOOLEAN_DEFAULT_CODEC = new BooleanCodecStringImpl(
            Optional.<BooleanTypeDefinition> absent());

    private static final DecimalCodecStringImpl DECIMAL64_DEFAULT_CODEC = new DecimalCodecStringImpl(
            Optional.<DecimalTypeDefinition> absent());

    private static final EmptyCodecStringImpl EMPTY_DEFAULT_CODEC = new EmptyCodecStringImpl(
            Optional.<EmptyTypeDefinition> absent());

    private static final Int8CodecStringImpl INT8_DEFAULT_CODEC = new Int8CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    private static final Int16CodecStringImpl INT16_DEFAULT_CODEC = new Int16CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    private static final Int32CodecStringImpl INT32_DEFAULT_CODEC = new Int32CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    private static final Int64CodecStringImpl INT64_DEFAULT_CODEC = new Int64CodecStringImpl(
            Optional.<IntegerTypeDefinition> absent());

    private static final StringCodecStringImpl STRING_DEFAULT_CODEC = new StringCodecStringImpl(
            Optional.<StringTypeDefinition> absent());

    private static final Uint8CodecStringImpl UINT8_DEFAULT_CODEC = new Uint8CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    private static final Uint16CodecStringImpl UINT16_DEFAULT_CODEC = new Uint16CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    private static final Uint32CodecStringImpl UINT32_DEFAULT_CODEC = new Uint32CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    private static final Uint64CodecStringImpl UINT64_DEFAULT_CODEC = new Uint64CodecStringImpl(
            Optional.<UnsignedIntegerTypeDefinition> absent());

    @Override
    public Class<J> getInputClass() {
        return inputClass;
    }

    protected TypeDefinitionAwareCodec(final Optional<T> typeDefinition, final Class<J> outputClass) {
        Preconditions.checkArgument(outputClass != null, "Output class must be specified.");
        this.typeDefinition = typeDefinition;
        this.inputClass = outputClass;
    }

    public Optional<T> getTypeDefinition() {
        return typeDefinition;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> from(final TypeDefinition typeDefinition) {
        return fromType(typeDefinition);
    }

    @SuppressWarnings("unchecked")
    public static final <T extends TypeDefinition<T>> TypeDefinitionAwareCodec<?, T> fromType(final T typeDefinition) {
        T superType = typeDefinition;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }

        @SuppressWarnings("rawtypes")
        TypeDefinitionAwareCodec codec = null;

        if (superType instanceof BinaryTypeDefinition) {
            codec = BINARY_DEFAULT_CODEC;
        } else if (superType instanceof BitsTypeDefinition) {
            codec = new BitsCodecStringImpl( Optional.of( (BitsTypeDefinition)superType ) );
        } else if (superType instanceof BooleanTypeDefinition) {
            codec = BOOLEAN_DEFAULT_CODEC;
        } else if (superType instanceof DecimalTypeDefinition) {
            codec = DECIMAL64_DEFAULT_CODEC;
        } else if (superType instanceof EmptyTypeDefinition) {
            codec = EMPTY_DEFAULT_CODEC;
        } else if (superType instanceof EnumTypeDefinition) {
            codec = new EnumCodecStringImpl( Optional.of( (EnumTypeDefinition)superType ) );
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
        } else if (superType instanceof UnionTypeDefinition) {
            codec = new UnionCodecStringImpl( Optional.of( (UnionTypeDefinition)superType ) );
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

        return codec;
    }

    public static class BooleanCodecStringImpl extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
            implements BooleanCodec<String> {

        protected BooleanCodecStringImpl(final Optional<BooleanTypeDefinition> typeDef) {
            super(typeDef, Boolean.class);
        }

        @Override
        public String serialize(final Boolean data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public Boolean deserialize(final String stringRepresentation) {
            return Boolean.valueOf(stringRepresentation);
        }
    }

    public static class Uint8CodecStringImpl extends TypeDefinitionAwareCodec<Short, UnsignedIntegerTypeDefinition>
            implements Uint8Codec<String> {

        protected Uint8CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
        }

        @Override
        public String serialize(final Short data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public Short deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Short.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Short.valueOf(stringRepresentation, base);
        }
    }

    public static class Uint16CodecStringImpl extends TypeDefinitionAwareCodec<Integer, UnsignedIntegerTypeDefinition>
            implements Uint16Codec<String> {
        protected Uint16CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
        }

        @Override
        public Integer deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Integer.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Integer.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Integer data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class Uint32CodecStringImpl extends TypeDefinitionAwareCodec<Long, UnsignedIntegerTypeDefinition>
            implements Uint32Codec<String> {

        protected Uint32CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
        }

        @Override
        public Long deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Long.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Long.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Long data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class Uint64CodecStringImpl extends
            TypeDefinitionAwareCodec<BigInteger, UnsignedIntegerTypeDefinition> implements Uint64Codec<String> {

        protected Uint64CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef, BigInteger.class);
        }

        @Override
        public BigInteger deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return new BigInteger(normalizeHexadecimal(stringRepresentation), base);
            }
            return new BigInteger(stringRepresentation, base);
        }

        @Override
        public String serialize(final BigInteger data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class StringCodecStringImpl extends TypeDefinitionAwareCodec<String, StringTypeDefinition> implements
            StringCodec<String> {

        protected StringCodecStringImpl(final Optional<StringTypeDefinition> typeDef) {
            super(typeDef, String.class);
        }

        @Override
        public String deserialize(final String stringRepresentation) {
            return stringRepresentation == null ? "" : stringRepresentation;
        }

        @Override
        public String serialize(final String data) {
            return data == null ? "" : data;
        }
    }

    public static class Int16CodecStringImpl extends TypeDefinitionAwareCodec<Short, IntegerTypeDefinition> implements
            Int16Codec<String> {

        protected Int16CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Short.class);
        }

        @Override
        public Short deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Short.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Short.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Short data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class Int32CodecStringImpl extends TypeDefinitionAwareCodec<Integer, IntegerTypeDefinition> implements
            Int32Codec<String> {

        protected Int32CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Integer.class);
        }

        @Override
        public Integer deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Integer.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Integer.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Integer data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class Int64CodecStringImpl extends TypeDefinitionAwareCodec<Long, IntegerTypeDefinition> implements
            Int64Codec<String> {

        protected Int64CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Long.class);
        }

        @Override
        public Long deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Long.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Long.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Long data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class Int8CodecStringImpl extends TypeDefinitionAwareCodec<Byte, IntegerTypeDefinition> implements
            Int8Codec<String> {

        protected Int8CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef, Byte.class);
        }

        @Override
        public Byte deserialize(final String stringRepresentation) {
            int base = provideBase(stringRepresentation);
            if (base == 16) {
                return Byte.valueOf(normalizeHexadecimal(stringRepresentation), base);
            }
            return Byte.valueOf(stringRepresentation, base);
        }

        @Override
        public String serialize(final Byte data) {
            return data == null ? "" : data.toString();
        }
    }

    public static class EmptyCodecStringImpl extends TypeDefinitionAwareCodec<Void, EmptyTypeDefinition> implements
            EmptyCodec<String> {

        protected EmptyCodecStringImpl(final Optional<EmptyTypeDefinition> typeDef) {
            super(typeDef, Void.class);
        }

        @Override
        public String serialize(final Void data) {
            return "";
        }

        @Override
        public Void deserialize(final String stringRepresentation) {
            Preconditions.checkArgument( Strings.isNullOrEmpty( stringRepresentation ),
                                         "The value must be empty" );
            return null;
        }
    }

    public static final class BinaryCodecStringImpl extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
            implements BinaryCodec<String> {

        protected BinaryCodecStringImpl(final Optional<BinaryTypeDefinition> typeDef) {
            super(typeDef, byte[].class);
        }

        @Override
        public String serialize(final byte[] data) {
            return data == null ? "" : BaseEncoding.base64().encode(data);
        }

        @Override
        public byte[] deserialize(final String stringRepresentation) {
            return stringRepresentation == null ? null : DatatypeConverter.parseBase64Binary(stringRepresentation);
        }
    }

    public static final class BitsCodecStringImpl extends TypeDefinitionAwareCodec<Set<String>, BitsTypeDefinition>
            implements BitsCodec<String> {

        public static final Joiner JOINER = Joiner.on(" ").skipNulls();
        public static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

        @SuppressWarnings("unchecked")
        protected BitsCodecStringImpl(final Optional<BitsTypeDefinition> typeDef) {
            super(typeDef, (Class<Set<String>>) ((Class<?>) Set.class));
        }

        @Override
        public String serialize(final Set<String> data) {
            return data == null ? "" : JOINER.join(data);
        }

        @Override
        public Set<String> deserialize(final String stringRepresentation) {
            if (stringRepresentation == null) {
                return ImmutableSet.of();
            }

            Iterable<String> strings = SPLITTER.split(stringRepresentation);

            if( getTypeDefinition().isPresent() ) {
                Set<String> allowedNames = Sets.newHashSet();
                for( BitsTypeDefinition.Bit bit: getTypeDefinition().get().getBits() ) {
                    allowedNames.add( bit.getName() );
                }

                for( String bit: strings ) {
                    if( !allowedNames.contains( bit ) ) {
                        throw new IllegalArgumentException(
                            "Invalid value \"" + bit + "\" for bits type. Allowed values are: " +
                            allowedNames );
                    }
                }
            }

            return ImmutableSet.copyOf(strings);
        }
    }

    public static class EnumCodecStringImpl extends TypeDefinitionAwareCodec<String, EnumTypeDefinition> implements
            EnumCodec<String> {

        protected EnumCodecStringImpl(final Optional<EnumTypeDefinition> typeDef) {
            super(typeDef, String.class);
        }

        @Override
        public String deserialize(final String stringRepresentation) {
            if( getTypeDefinition().isPresent() ) {
                Set<String> allowedNames = Sets.newHashSet();
                for( EnumPair pair: getTypeDefinition().get().getValues() ) {
                    allowedNames.add( pair.getName() );
                }

                if( !allowedNames.contains( stringRepresentation ) ) {
                    throw new IllegalArgumentException(
                        "Invalid value \"" + stringRepresentation + "\" for enum type. Allowed values are: " +
                        allowedNames );
                }
            }

            return stringRepresentation;
        }

        @Override
        public String serialize(final String data) {
            return data == null ? "" : data;
        }
    }

    public static class DecimalCodecStringImpl extends TypeDefinitionAwareCodec<BigDecimal, DecimalTypeDefinition>
            implements DecimalCodec<String> {

        protected DecimalCodecStringImpl(final Optional<DecimalTypeDefinition> typeDef) {
            super(typeDef, BigDecimal.class);
        }

        @Override
        public String serialize(final BigDecimal data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public BigDecimal deserialize(final String stringRepresentation) {
            Preconditions.checkArgument( stringRepresentation != null , "Input cannot be null" );
            return new BigDecimal(stringRepresentation);
        }
    }

    public static class UnionCodecStringImpl extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition> implements
            UnionCodec<String> {

        protected UnionCodecStringImpl(final Optional<UnionTypeDefinition> typeDef) {
            super(typeDef, Object.class);
        }

        @Override
        public String serialize(final Object data) {
            return data == null ? "" : data.toString();
        }

        @Override
        public Object deserialize(final String stringRepresentation) {
            if( getTypeDefinition().isPresent() ) {
                boolean valid = false;
                for( TypeDefinition<?> type: getTypeDefinition().get().getTypes() ) {
                    TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec = from( type );
                    if( typeAwareCodec == null ) {
                        // This is a type for which we have no codec (eg identity ref) so we'll say it's valid
                        // but we'll continue in case there's another type for which we do have a codec.
                        valid = true;
                        continue;
                    }

                    try {
                        typeAwareCodec.deserialize( stringRepresentation );
                        valid = true;
                        break;
                    }
                    catch( Exception e ) {
                        // invalid - try the next union type.
                    }
                }

                if( !valid ) {
                    throw new IllegalArgumentException(
                                        "Invalid value \"" + stringRepresentation + "\" for union type." );
                }
            }

            return stringRepresentation;
        }
    }
}
