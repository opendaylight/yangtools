/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import java.math.BigDecimal;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
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
import org.opendaylight.yangtools.yang.model.util.DerivedType;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {



    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;



    private static final BinaryCodecStringImpl BINARY_DEFAULT_CODEC = new BinaryCodecStringImpl(
            Optional.<BinaryTypeDefinition> absent());

    private static final BooleanCodecStringImpl BOOLEAN_DEFAULT_CODEC = new BooleanCodecStringImpl(
            Optional.<BooleanTypeDefinition> absent());

    private static final DecimalCodecStringImpl DECIMAL64_DEFAULT_CODEC = new DecimalCodecStringImpl(
            Optional.<DecimalTypeDefinition> absent());

    private static final EmptyCodecStringImpl EMPTY_DEFAULT_CODEC = new EmptyCodecStringImpl(
            Optional.<EmptyTypeDefinition> absent());

    private static final StringCodecStringImpl STRING_DEFAULT_CODEC = new StringCodecStringImpl(
            Optional.<StringTypeDefinition> absent());

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
        final T normalizedType = (T) DerivedType.from(typeDefinition);
        @SuppressWarnings("rawtypes")
        TypeDefinitionAwareCodec codec = null;

        if (normalizedType instanceof BinaryTypeDefinition) {
            codec = BINARY_DEFAULT_CODEC;
        } else if (normalizedType instanceof BitsTypeDefinition) {
            codec = new BitsCodecStringImpl( Optional.of( (BitsTypeDefinition)normalizedType ) );
        } else if (normalizedType instanceof BooleanTypeDefinition) {
            codec = BOOLEAN_DEFAULT_CODEC;
        } else if (normalizedType instanceof DecimalTypeDefinition) {
            codec = DECIMAL64_DEFAULT_CODEC;
        } else if (normalizedType instanceof EmptyTypeDefinition) {
            codec = EMPTY_DEFAULT_CODEC;
        } else if (normalizedType instanceof EnumTypeDefinition) {
            codec = new EnumCodecStringImpl( Optional.of( (EnumTypeDefinition)normalizedType ) );
        } else if (normalizedType instanceof IntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((IntegerTypeDefinition) normalizedType);
        } else if (normalizedType instanceof StringTypeDefinition) {
            codec = STRING_DEFAULT_CODEC;
        } else if (normalizedType instanceof UnionTypeDefinition) {
            codec = new UnionCodecStringImpl( Optional.of( (UnionTypeDefinition)normalizedType ) );
        } else if (normalizedType instanceof UnsignedIntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((UnsignedIntegerTypeDefinition) normalizedType);
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

    @Deprecated
    public static class Uint8CodecStringImpl extends Uint8StringCodec {

        protected Uint8CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef);
        }
    }

    @Deprecated
    public static class Uint16CodecStringImpl extends Uint16StringCodec {
        protected Uint16CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef);
        }
    }

    @Deprecated
    public static class Uint32CodecStringImpl extends Uint32StringCodec {

        protected Uint32CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static class Uint64CodecStringImpl extends Uint64StringCodec {

        protected Uint64CodecStringImpl(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
            super(typeDef);
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

    @Deprecated
    public static class Int16CodecStringImpl extends Int16StringCodec {

        protected Int16CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static class Int32CodecStringImpl extends Int32StringCodec {

        protected Int32CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static class Int64CodecStringImpl extends Int64StringCodec {

        protected Int64CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static class Int8CodecStringImpl extends Int8StringCodec {

        protected Int8CodecStringImpl(final Optional<IntegerTypeDefinition> typeDef) {
            super(typeDef);
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

            final Iterable<String> strings = SPLITTER.split(stringRepresentation);

            if( getTypeDefinition().isPresent() ) {
                final Set<String> allowedNames = Sets.newHashSet();
                for( final BitsTypeDefinition.Bit bit: getTypeDefinition().get().getBits() ) {
                    allowedNames.add( bit.getName() );
                }

                for( final String bit: strings ) {
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
                final Set<String> allowedNames = Sets.newHashSet();
                for( final EnumPair pair: getTypeDefinition().get().getValues() ) {
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
                for( final TypeDefinition<?> type: getTypeDefinition().get().getTypes() ) {
                    final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwareCodec = from( type );
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
                    catch( final Exception e ) {
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
