/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BitsStringCodec extends TypeDefinitionAwareCodec<Set<String>, BitsTypeDefinition>
        implements BitsCodec<String> {

    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private final Set<String> validBits;

    @SuppressWarnings("unchecked")
    private BitsStringCodec(final Optional<BitsTypeDefinition> typeDef) {
        super(typeDef, (Class<Set<String>>) (Class<?>) Set.class);
        if (typeDef.isPresent()) {
            validBits = ImmutableSet.copyOf(Collections2.transform(typeDef.get().getBits(), Bit::getName));
        } else {
            validBits = null;
        }
    }

    public static BitsStringCodec from(final BitsTypeDefinition type) {
        return new BitsStringCodec(Optional.of(type));
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

        final Set<String> strings = ImmutableSet.copyOf(SPLITTER.split(stringRepresentation));
        if (validBits == null) {
            // No schema available, use what we have
            return strings;
        }

        // Normalize strings to schema first, retaining definition order
        final List<String> sorted = new ArrayList<>(strings.size());
        for (final String bit : validBits) {
            if (strings.contains(bit)) {
                sorted.add(bit);
            }
        }

        // Check sizes, if the normalized set does not match non-normalized size, non-normalized strings contain
        // an invalid bit.
        if (sorted.size() != strings.size()) {
            for (final String bit : strings) {
                Preconditions.checkArgument(validBits.contains(bit),
                    "Invalid value '%s' for bits type. Allowed values are: %s", bit, validBits);
            }
        }

        // In case all valid bits have been specified, retain the set we have created for this codec
        return sorted.size() == validBits.size() ? validBits : ImmutableSet.copyOf(sorted);
    }
}