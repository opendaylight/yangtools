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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DerivedType;

public abstract class TypeDefinitionAwareCodec<J, T extends TypeDefinition<T>> implements DataStringCodec<J> {

    private static final TypeDefinitionAwareCodec<?,?> EMPTY_DEFAULT_CODEC = new EmptyStringCodec(Optional.<EmptyTypeDefinition>absent());
    private final Optional<T> typeDefinition;
    private final Class<J> inputClass;

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
            codec = BinaryStringCodec.from((BinaryTypeDefinition)normalizedType);
        } else if (normalizedType instanceof BitsTypeDefinition) {
            codec = BitsStringCodec.from((BitsTypeDefinition)normalizedType);
        } else if (normalizedType instanceof BooleanTypeDefinition) {
            codec = BooleanStringCodec.from((BooleanTypeDefinition)normalizedType);
        } else if (normalizedType instanceof DecimalTypeDefinition) {
            codec = DecimalStringCodec.from((DecimalTypeDefinition)normalizedType);
        } else if (normalizedType instanceof EmptyTypeDefinition) {
            codec = EMPTY_DEFAULT_CODEC;
        } else if (normalizedType instanceof EnumTypeDefinition) {
            codec = EnumStringCodec.from((EnumTypeDefinition)normalizedType);
        } else if (normalizedType instanceof IntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((IntegerTypeDefinition) normalizedType);
        } else if (normalizedType instanceof StringTypeDefinition) {
            codec = StringStringCodec.from((StringTypeDefinition)normalizedType);
        } else if (normalizedType instanceof UnionTypeDefinition) {
            codec = UnionStringCodec.from((UnionTypeDefinition)normalizedType);
        } else if (normalizedType instanceof UnsignedIntegerTypeDefinition) {
            codec = AbstractIntegerStringCodec.from((UnsignedIntegerTypeDefinition) normalizedType);
        }
        return codec;
    }

    @Deprecated
    public static class BooleanCodecStringImpl extends BooleanStringCodec{
        protected BooleanCodecStringImpl(final Optional<BooleanTypeDefinition> typeDef) {
            super(typeDef);
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

    @Deprecated
    public static class StringCodecStringImpl extends StringStringCodec {
        protected StringCodecStringImpl(final Optional<StringTypeDefinition> typeDef) {
            super(typeDef.get());
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

    @Deprecated
    public static class EmptyCodecStringImpl extends EmptyStringCodec {
        protected EmptyCodecStringImpl(final Optional<EmptyTypeDefinition> typeDef) {
            super(typeDef);
        }
    }

    @Deprecated
    public static final class BinaryCodecStringImpl extends BinaryStringCodec {
        protected BinaryCodecStringImpl(final Optional<BinaryTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static final class BitsCodecStringImpl extends BitsStringCodec {

        public static final Joiner JOINER = Joiner.on(" ").skipNulls();
        public static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

        protected BitsCodecStringImpl(final Optional<BitsTypeDefinition> typeDef) {
            super(typeDef);
        }
    }

    @Deprecated
    public static class EnumCodecStringImpl extends EnumStringCodec {

        protected EnumCodecStringImpl(final Optional<EnumTypeDefinition> typeDef) {
            super(typeDef);
        }

    }

    @Deprecated
    public static class DecimalCodecStringImpl extends DecimalStringCodec {
        protected DecimalCodecStringImpl(final Optional<DecimalTypeDefinition> typeDef) {
            super(typeDef);
        }
    }

    @Deprecated
    public static class UnionCodecStringImpl extends UnionStringCodec {
        protected UnionCodecStringImpl(final Optional<UnionTypeDefinition> typeDef) {
            super(typeDef);
        }
    }
}
