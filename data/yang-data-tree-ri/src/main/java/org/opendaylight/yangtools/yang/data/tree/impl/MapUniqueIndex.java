/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

/**
 * A steady-state version of a {@code unique} index.
 */
@NonNullByDefault
sealed interface MapUniqueIndex extends Immutable permits MapUniqueIndex1, MapUniqueIndexN {
    /**
     * {@return an empty {@link Builder}}
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Look up the {@link MapNode} key that corresponds to the entry having specified set of {@code unique} leaf values.
     * The set can be either a single leaf value, including {@code byte[]}, or a {@link UniqueValues} instance.
     *
     * @param uniqueValues the unique values
     * @return the key, or {@code null} if the combination of values is not associated with an entry.
     */
    @Nullable NodeIdentifierWithPredicates lookupKey(Object uniqueValues);

    /**
     * {@return a {@link Builder} initialized with the contents of this index}
     */
    default Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * A {@link MapUniqueIndex} undergoing modification. The theory of operation is simple: we have a base index, which
     * may be empty and we perform removals and additions on it in random order. That means that for some period of time
     * we may be tracking more keys for a value. That's a thing we settle during build() time.
     */
    final class Builder implements Mutable {
        private final ArrayList<Entry<Object, NodeIdentifierWithPredicates>> remove = new ArrayList<>();
        private final ArrayList<Entry<Object, NodeIdentifierWithPredicates>> add = new ArrayList<>();
        private final MapUniqueIndex base;

        private Builder() {
            base = MapUniqueIndexN.EMPTY;
        }

        private Builder(final MapUniqueIndex base) {
            this.base = requireNonNull(base);
        }

        Builder addUniqueValues(final Object uniqueValues, final NodeIdentifierWithPredicates key) {
            add.add(Map.entry(BinaryValue.wrap(uniqueValues), key));
            return this;
        }

        Builder removeUniqueValues(final Object uniqueValues, final NodeIdentifierWithPredicates key) {
            remove.add(Map.entry(BinaryValue.wrap(uniqueValues), key));
            return this;
        }

        <X extends Exception> MapUniqueIndex build(final Supplier<X> exceptionSupplier) throws X {
            if (add.isEmpty() && remove.isEmpty()) {
                // No change
                return base;
            }

            final var mapAdaptor = MapAdaptor.getDefaultInstance();
            final var map = switch (base) {
                case MapUniqueIndexN many -> mapAdaptor.takeSnapshot(many.map());
                case MapUniqueIndex1 one -> {
                    final var tmp = mapAdaptor.<Object, NodeIdentifierWithPredicates>initialSnapshot(2);
                    tmp.put(BinaryValue.wrap(one.uniqueValues()), one.key());
                    yield tmp;
                }
            };

            for (var entry : remove) {
                if (!map.remove(entry.getKey(), entry.getValue())) {
                    throw new VerifyException("Failed to remove " + entry + " from " + map);
                }
            }
            for (var entry : add) {
                final var prev = map.put(entry.getKey(), entry.getValue());
                if (prev != null) {
                    // FIXME: more details
                    // FIXME: multiple collisions
                    throw exceptionSupplier.get();
                }
            }

            return switch (map.size()) {
                case 0 -> MapUniqueIndexN.EMPTY;
                case 1 -> {
                    final var entry = map.entrySet().iterator().next();
                    yield new MapUniqueIndex1(BinaryValue.unwrap(entry.getKey()), entry.getValue());
                }
                default -> new MapUniqueIndexN(MapAdaptor.getDefaultInstance().optimize(map));
            };
        }
    }
}
