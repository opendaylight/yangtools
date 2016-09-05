/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitsStringCodec extends TypeDefinitionAwareCodec<Set<String>, BitsTypeDefinition>
        implements BitsCodec<String> {

    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private final Set<String> bits;

    @SuppressWarnings("unchecked")
    private BitsStringCodec(final Optional<BitsTypeDefinition> typeDef) {
        super(typeDef, (Class<Set<String>>) ((Class<?>) Set.class));
        if (typeDef.isPresent()) {
            final List<Bit> yangBits = typeDef.get().getBits();
            final Set<String> bitsBuilder = Sets.newHashSet();
            for (final Bit bit : yangBits) {
                bitsBuilder.add(bit.getName());
            }
            bits = ImmutableSet.copyOf(bitsBuilder);
        } else {
            bits = null;
        }
    }

    static TypeDefinitionAwareCodec<?, BitsTypeDefinition> from(final BitsTypeDefinition normalizedType) {
        return new BitsStringCodec(Optional.fromNullable(normalizedType));
    }

    @Override
    public String serialize(final Set<String> data) {
        if (data == null) {
            return "";
        }
        return JOINER.join(Collections.sort(new ArrayList<String>(data)));
    }

    @Override
    public Set<String> deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            return ImmutableSet.of();
        }

        final Iterable<String> strings = SPLITTER.split(stringRepresentation);
        validate(strings);
        return ImmutableSet.copyOf(strings);
    }

    private void validate(final Iterable<String> strings) {
        if (bits != null) {
            for (final String bit : strings) {
                Preconditions.checkArgument(bits.contains(bit),
                        "Invalid value '%s' for bits type. Allowed values are: %s", bit, bits);
            }
        }
    }
}