/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Qualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractNormalizedNodeParsingCodecFactory;
import org.opendaylight.yangtools.yang.data.util.codec.CodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.LazyCodecCache;
import org.opendaylight.yangtools.yang.data.util.codec.NormalizedNodeParserException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

/**
 * Factory for creating JSON equivalents of codecs. Each instance of this object is bound to
 * a particular {@link EffectiveModelContext}, but can be reused by multiple {@link JSONNormalizedNodeStreamWriter}s.
 */
public abstract sealed class JSONCodecFactory extends AbstractNormalizedNodeParsingCodecFactory<JSONCodec<?>> {
    @Deprecated(since = "12.0.0", forRemoval = true)
    static final class Lhotka02 extends JSONCodecFactory {
        Lhotka02(final @NonNull EffectiveModelContext context, final @NonNull CodecCache<JSONCodec<?>> cache) {
            super(context, cache, JSONInstanceIdentifierCodec.Lhotka02::new);
        }

        @Override
        Lhotka02 rebaseTo(final EffectiveModelContext newSchemaContext, final CodecCache<JSONCodec<?>> newCache) {
            return new Lhotka02(newSchemaContext, newCache);
        }

        @Override
        JSONCodec<?> wrapDecimalCodec(final DecimalStringCodec decimalCodec) {
            return new NumberJSONCodec<>(decimalCodec);
        }

        @Override
        JSONCodec<?> wrapIntegerCodec(final AbstractIntegerStringCodec<?, ?> integerCodec) {
            return new NumberJSONCodec<>(integerCodec);
        }
    }

    static final class RFC7951 extends JSONCodecFactory {
        RFC7951(final @NonNull  EffectiveModelContext context, final @NonNull CodecCache<JSONCodec<?>> cache) {
            super(context, cache, JSONInstanceIdentifierCodec.RFC7951::new);
        }

        @Override
        RFC7951 rebaseTo(final EffectiveModelContext newSchemaContext, final CodecCache<JSONCodec<?>> newCache) {
            return new RFC7951(newSchemaContext, newCache);
        }

        @Override
        JSONCodec<?> wrapDecimalCodec(final DecimalStringCodec decimalCodec) {
            return new QuotedJSONCodec<>(decimalCodec);
        }

        @Override
        JSONCodec<?> wrapIntegerCodec(final AbstractIntegerStringCodec<?, ?> integerCodec) {
            return new QuotedJSONCodec<>(integerCodec);
        }
    }

    private final @NonNull JSONInstanceIdentifierCodec iidCodec;

    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR",
        justification = "https://github.com/spotbugs/spotbugs/issues/1867")
    private JSONCodecFactory(final @NonNull EffectiveModelContext context,
            final @NonNull CodecCache<JSONCodec<?>> cache,
            final BiFunction<EffectiveModelContext, JSONCodecFactory, @NonNull JSONInstanceIdentifierCodec> iidCodec) {
        super(context, cache);
        this.iidCodec = verifyNotNull(iidCodec.apply(context, this));
    }

    @Override
    protected final JSONCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedJSONCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanJSONCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedJSONCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return wrapDecimalCodec(DecimalStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyJSONCodec.INSTANCE;
    }

    @Override
    protected final JSONCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedJSONCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<QName> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefJSONCodec(getEffectiveModelContext(), module);
    }

    @Override
    protected final JSONCodec<YangInstanceIdentifier> instanceIdentifierCodec(
            final InstanceIdentifierTypeDefinition type) {
        return iidCodec;
    }

    @Override
    public JSONCodec<YangInstanceIdentifier> instanceIdentifierCodec() {
        return iidCodec;
    }

    @Override
    protected final JSONCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> int64Codec(final Int64TypeDefinition type) {
        return wrapIntegerCodec(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedJSONCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberJSONCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return wrapIntegerCodec(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected final JSONCodec<?> unionCodec(final UnionTypeDefinition type, final List<JSONCodec<?>> codecs) {
        return UnionJSONCodec.create(type, codecs);
    }

    @Override
    protected final JSONCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullJSONCodec.INSTANCE;
    }

    // Returns a one-off factory for the purposes of normalizing an anydata tree.
    //
    // FIXME: 7.0.0: this is really ugly, as we should be able to tell if the new context is the same as ours and
    //               whether our cache is thread-safe -- in which case we should just return this.
    //               The supplier/cache/factory layout needs to be reworked so that this call ends up being equivalent
    //               to JSONCodecFactorySupplier.getShared() in case this factory is not thread safe.
    //
    //               The above is not currently possible, as we cannot reference JSONCodecFactorySupplier from the
    //               factory due to that potentially creating a circular reference.
    final JSONCodecFactory rebaseTo(final EffectiveModelContext newSchemaContext) {
        return rebaseTo(newSchemaContext, new LazyCodecCache<>());
    }

    abstract JSONCodecFactory rebaseTo(EffectiveModelContext newSchemaContext, CodecCache<JSONCodec<?>> newCache);

    abstract JSONCodec<?> wrapDecimalCodec(DecimalStringCodec decimalCodec);

    abstract JSONCodec<?> wrapIntegerCodec(AbstractIntegerStringCodec<?, ?> integerCodec);


    @Override
    protected final NormalizedNode parseDatastoreImpl(final XMLNamespace rootNamespace,  final Qualified rootName,
            final InputStream stream) throws IOException, NormalizedNodeParserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final NormalizedNode parseDataImpl(final SchemaInferenceStack stack, final InputStream stream)
            throws IOException, NormalizedNodeParserException {
        // Point to parent node
        stack.exit();

        final var holder = new NormalizationResultHolder();
        try (var writer = ImmutableNormalizedNodeStreamWriter.from(holder)) {
            try (var parser = JsonParserStream.create(writer, this, stack.toInference())) {
                try (var reader = newJsonReader(stream)) {
                    parser.parse(reader);
                } catch (JsonParseException e) {
                    throw NormalizedNodeParserException.ofCause(e);
                }
            }
        }
        return holder.getResult().data();
    }

    @Override
    protected NormalizedNode parseChildDataImpl(final EffectiveStatementInference inference, final InputStream stream)
            throws IOException, NormalizedNodeParserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected NormalizedNode parseInputOutput(final Inference inference, final InputStream stream)
            throws IOException, NormalizedNodeParserException {
        // TODO Auto-generated method stub
        return null;
    }

    private static JsonReader newJsonReader(final InputStream stream) {
        return new JsonReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
