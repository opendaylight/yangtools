/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.codec.AbstractStringConverter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitsStringConverter extends AbstractStringConverter<Set<String>, BitsTypeDefinition> {
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private final ImmutableSet<String> validBits;

    @SuppressWarnings("unchecked")
    BitsStringConverter(final BitsTypeDefinition typeDef) {
        super((Class<Set<String>>) (Class<?>) Set.class, typeDef);
        validBits = typeDef.getBits().stream()
            .map(Bit::getName)
            .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    protected Set<String> normalizeFromString(final BitsTypeDefinition typeDef, final String str)
            throws NormalizationException {
        final var strings = ImmutableSet.copyOf(SPLITTER.split(str));

        // Normalize strings to schema first, retaining definition order
        final var sorted = new ArrayList<String>(strings.size());
        for (var bit : validBits) {
            if (strings.contains(bit)) {
                sorted.add(bit);
            }
        }

        checkBits(sorted, strings);
        // In case all valid bits have been specified, retain the set we have created for this codec
        return sorted.size() == validBits.size() ? validBits : ImmutableSet.copyOf(sorted);
    }

    @Override
    protected String canonizeToString(final BitsTypeDefinition typeDef, final Set<String> obj)
            throws NormalizationException {
        // Re-establish order if needed
        final var sorted = new ArrayList<String>();
        for (var bit : validBits) {
            if (obj.contains(bit)) {
                sorted.add(bit);
            }
        }

        checkBits(sorted, obj);
        return JOINER.join(sorted);
    }

    private void checkBits(final ArrayList<String> sorted, final Set<String> strings) throws NormalizationException {
        // Check sizes, if the normalized set does not match non-normalized size, non-normalized strings contain
        // an invalid bit.
        if (sorted.size() != strings.size()) {
            for (var bit : strings) {
                // FIXME: multi-error exception
                if (!validBits.contains(bit)) {
                    throw NormalizationException.ofMessage(
                        "Invalid value '" + bit + "' for bits type. Allowed values are: " + validBits);
                }
            }
        }
    }
}