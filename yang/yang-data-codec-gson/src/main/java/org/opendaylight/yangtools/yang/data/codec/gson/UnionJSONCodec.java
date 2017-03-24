/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Union codec based with a pre-calculated set of sub-types.
 *
 * @author Robert Varga
 *
 * @param <T> Data representation type
 */
abstract class UnionJSONCodec<T> implements JSONCodec<T> {
    private static final class Diverse extends UnionJSONCodec<Object> {
        Diverse(final List<JSONCodec<?>> codecs) {
            super(codecs);
        }

        @Override
        public Class<Object> getDataClass() {
            return Object.class;
        }
    }

    private static final class SingleType<T> extends UnionJSONCodec<T> {
        private final Class<T> dataClass;

        SingleType(final Class<T> dataClass, final List<JSONCodec<?>> codecs) {
            super(codecs);
            this.dataClass = Preconditions.checkNotNull(dataClass);
        }

        @Override
        public Class<T> getDataClass() {
            return dataClass;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(UnionJSONCodec.class);

    private final List<JSONCodec<?>> codecs;

    UnionJSONCodec(final List<JSONCodec<?>> codecs) {
        this.codecs = ImmutableList.copyOf(codecs);
    }

    static UnionJSONCodec<?> create(final UnionTypeDefinition type, final List<JSONCodec<?>> codecs) {
        final Iterator<JSONCodec<?>> it = codecs.iterator();
        Verify.verify(it.hasNext(), "Union %s has no subtypes", type);

        Class<?> dataClass = it.next().getDataClass();
        while (it.hasNext()) {
            final Class<?> next = it.next().getDataClass();
            if (!dataClass.equals(next)) {
                LOG.debug("Type {} has diverse data classes: {} and {}", type, dataClass, next);
                return new Diverse(codecs);
            }
        }

        LOG.debug("Type {} has single data class {}", type, dataClass);
        return new SingleType<>(dataClass, codecs);
    }

    @Override
    public final T deserializeString(final String input) {
        for (JSONCodec<?> codec : codecs) {
            final Object ret;
            try {
                ret = codec.deserializeString(input);
            } catch (RuntimeException e) {
                LOG.debug("Codec {} did not accept input '{}'", codec, input, e);
                continue;
            }

            return getDataClass().cast(ret);
        }

        throw new IllegalArgumentException("Input '" + input +"'  did not match any codecs");
    }

    @Override
    public final void serializeToWriter(final JsonWriter writer, final T value) throws IOException {
        for (JSONCodec<?> codec : codecs) {
            if (!codec.getDataClass().isInstance(value)) {
                LOG.debug("Codec {} cannot accept input {}, skipping it", codec, value);
                continue;
            }

            @SuppressWarnings("unchecked")
            final JSONCodec<Object> objCodec = (JSONCodec<Object>) codec;
            try {
                objCodec.serializeToWriter(writer, value);
                return;
            } catch (RuntimeException e) {
                LOG.debug("Codec {} failed to serialize {}", codec, value, e);
            }
        }

        throw new IllegalArgumentException("No codecs could serialize" + value);
    }
}
