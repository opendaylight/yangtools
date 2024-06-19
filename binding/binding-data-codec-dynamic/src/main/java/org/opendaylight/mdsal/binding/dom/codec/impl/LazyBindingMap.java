/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.MapCodecContext.Unordered;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazily-populated Map of binding DTOs. This implementation acts as the main entry point, so that we can decide on the
 * translation strategy we are going to use. We make that decision based on the first method that touches the mappings
 * (or materializes a view).
 *
 * @param <K> key type
 * @param <V> value type
 */
final class LazyBindingMap<K extends Key<V>, V extends DataObject & KeyAware<K>>
        extends AbstractMap<K, V> implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(LazyBindingMap.class);
    private static final String LAZY_CUTOFF_PROPERTY =
            "org.opendaylight.mdsal.binding.dom.codec.impl.LazyBindingMap.max-eager-elements";
    private static final int DEFAULT_LAZY_CUTOFF = 1;
    @VisibleForTesting
    static final int LAZY_CUTOFF;

    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(LazyBindingMap.class, "state",
                State.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }

        final int value = Integer.getInteger(LAZY_CUTOFF_PROPERTY, DEFAULT_LAZY_CUTOFF);
        if (value < 0) {
            LOG.info("Lazy population of maps disabled");
            LAZY_CUTOFF = Integer.MAX_VALUE;
        } else {
            LOG.info("Using lazy population for maps larger than {} element(s)", value);
            LAZY_CUTOFF = value;
        }
    }

    private final @NonNull Unordered<K, V> codec;
    private final @NonNull MapNode mapNode;

    // Used via VarHandle above
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile State<K, V> state;

    private LazyBindingMap(final Unordered<K, V> codec, final MapNode mapNode) {
        this.codec = requireNonNull(codec);
        this.mapNode = requireNonNull(mapNode);
    }

    static <K extends Key<V>, V extends DataObject & KeyAware<K>> @NonNull Map<K, V> of(final Unordered<K, V> codec,
            final MapNode mapNode, final int size) {
        if (size == 1) {
            // Do not bother with lazy instantiation in case of a singleton
            final V entry = codec.createBindingProxy(mapNode.body().iterator().next());
            return Map.of(entry.key(), entry);
        }
        return size > LAZY_CUTOFF ? new LazyBindingMap<>(codec, mapNode) : eagerMap(codec, mapNode, size);
    }

    private static <K extends Key<V>, V extends DataObject & KeyAware<K>> @NonNull Map<K, V> eagerMap(
            final Unordered<K, V> codec, final MapNode mapNode, final int size) {
        final Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(size);
        for (MapEntryNode node : mapNode.body()) {
            final V entry = codec.createBindingProxy(node);
            builder.put(entry.key(), entry);
        }
        return builder.build();
    }

    @Override
    public int size() {
        return mapNode.size();
    }

    @Override
    public V remove(final Object key) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw uoe();
    }

    @Override
    public void clear() {
        throw uoe();
    }

    @Override
    public boolean containsKey(final Object key) {
        return lookupState().containsKey(requireNonNull(key));
    }

    @Override
    public boolean containsValue(final Object value) {
        /*
         * This implementation relies on the relationship specified by KeyAware/Key and its use in binding objects. The
         * key is a wrapper object composed of a subset (or all) properties in the value, i.e. we have a partial index.
         *
         * Instead of performing an O(N) search, we extract the key from the value, look the for the corresponding
         * mapping. If we find a mapping we check if the mapped value equals the the value being looked up.
         *
         * Note we prefer throwing ClassCastException/NullPointerException when presented with null or an object which
         * cannot possibly be contained in this map.
         */
        final V cast = codec.getBindingClass().cast(requireNonNull(value));
        final V found = get(cast.key());
        return found != null && cast.equals(found);
    }

    @Override
    public V get(final Object key) {
        return lookupState().get(requireNonNull(key));
    }

    @Override
    public Set<K> keySet() {
        return iterState().keySet();
    }

    @Override
    public Collection<V> values() {
        return iterState().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return iterState().entrySet();
    }

    @NonNull V createValue(final MapEntryNode node) {
        return codec.createBindingProxy(node);
    }

    Optional<V> lookupValue(final @NonNull Object key) {
        final NodeIdentifierWithPredicates childId = codec.serialize((Key<?>) key);
        return mapNode.findChildByArg(childId).map(codec::createBindingProxy);
    }

    @NonNull MapNode mapNode() {
        return mapNode;
    }

    private @NonNull State<K, V> lookupState() {
        final State<K, V> local;
        return (local = (State<K, V>) STATE.getAcquire(this)) != null ? local : loadLookup();
    }

    private @NonNull State<K, V> iterState() {
        final State<K, V> local;
        return (local = (State<K, V>) STATE.getAcquire(this)) != null ? local : loadIter();
    }

    @SuppressWarnings("unchecked")
    private @NonNull State<K, V> loadLookup() {
        final State<K, V> ret = new LazyBindingMapLookupState<>(this);
        final Object witness;
        return (witness = STATE.compareAndExchangeRelease(this, null, ret)) == null ? ret : (State<K, V>) witness;
    }

    @SuppressWarnings("unchecked")
    private @NonNull State<K, V> loadIter() {
        final State<K, V> ret = new LazyBindingMapIterState<>(this);
        final Object witness;
        return (witness = STATE.compareAndExchangeRelease(this, null, ret)) == null ? ret : (State<K, V>) witness;
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Modification is not supported");
    }

    abstract static class State<K extends Key<V>, V extends DataObject & KeyAware<K>> {
        abstract boolean containsKey(@NonNull Object key);

        abstract V get(@NonNull Object key);

        abstract @NonNull Set<K> keySet();

        abstract @NonNull Collection<V> values();

        abstract @NonNull Set<Entry<K, V>> entrySet();
    }
}
