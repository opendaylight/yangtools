/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import java.util.Objects;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * A steady-state version of a {@code unique} index.
 */
@NonNullByDefault
sealed interface UniqueIndex {

    // Key == may be byte[]
    @Nullable NodeIdentifierWithPredicates lookupKey(Object uniqueValues);

    default Builder toBuilder() {
        return new Builder(this);
    }

    static Builder builder() {
        return new Builder(Builder.ManyUniqueIndex.EMPTY);
    }

    final class Builder {
        // The theory of operation is simple: we have a base index, which may be empty and we perform removals and
        // additions on it in random order. That means that for some period of time we may be tracking more keys for
        // a value. That's a thing we settle during build() time.
        private final ArrayList<Entry<Object, NodeIdentifierWithPredicates>> remove = new ArrayList<>();
        private final ArrayList<Entry<Object, NodeIdentifierWithPredicates>> add = new ArrayList<>();
        private final UniqueIndex base;

        private Builder(final UniqueIndex base) {
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

        <X extends Exception> UniqueIndex build(final Supplier<X> exceptionSupplier) throws X {
            if (add.isEmpty() && remove.isEmpty()) {
                // No change
                return base;
            }

            final Map<Object, NodeIdentifierWithPredicates> map;
            if (base instanceof OneUniqueIndex one) {
                map = MapAdaptor.getDefaultInstance().initialSnapshot(2);
                map.put(BinaryValue.wrap(one.uniqueValues), one.key);
            } else if (base instanceof ManyUniqueIndex many) {
                map = MapAdaptor.getDefaultInstance().takeSnapshot(many.map);
            } else {
                throw new VerifyException("Unhandled base " + base);
            }

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
                case 0 -> ManyUniqueIndex.EMPTY;
                case 1 -> {
                    final var entry = map.entrySet().iterator().next();
                    yield new OneUniqueIndex(BinaryValue.unwrap(entry.getKey()), entry.getValue());
                }
                default -> new ManyUniqueIndex(MapAdaptor.getDefaultInstance().optimize(map));
            };
        }

        private record OneUniqueIndex(Object uniqueValues, NodeIdentifierWithPredicates key) implements UniqueIndex {
            OneUniqueIndex {
                requireNonNull(uniqueValues);
                requireNonNull(key);
            }

            @Override
            public @Nullable NodeIdentifierWithPredicates lookupKey(final Object uniqueValues) {
                return Objects.deepEquals(this.uniqueValues, requireNonNull(uniqueValues)) ? key : null;
            }
        }

        private record ManyUniqueIndex(Map<Object, NodeIdentifierWithPredicates> map) implements UniqueIndex {
            static final UniqueIndex EMPTY = new ManyUniqueIndex(Map.of());

            ManyUniqueIndex {
                requireNonNull(map);
            }

            @Override
            public @Nullable NodeIdentifierWithPredicates lookupKey(final Object uniqueValues) {
                return map.get(requireNonNull(BinaryValue.wrap(uniqueValues)));
            }
        }
    }
}
