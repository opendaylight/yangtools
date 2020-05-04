/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.UnaryOperator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazily-populated List implementation backed by NormalizedNodes. This implementation defers creating Binding objects
 * until they are actually needed, caching them in a pre-allocated array.
 *
 * <p>
 * The cost of this deferred instantiation is two-fold:
 * <ul>
 *   <li>each access issues a {@link VarHandle#getAcquire(Object...)} load and a class equality check</li>
 *   <li>initial load additionally incurs a {@link VarHandle#compareAndExchangeRelease(Object...)} store</li>
 * </ul>
 *
 * @param <E> the type of elements in this list
 */
final class LazyBindingList<E extends DataObject> extends AbstractList<E> implements Immutable, RandomAccess {
    // Object array access variable handle
    static final VarHandle OBJ_AA = MethodHandles.arrayElementVarHandle(Object[].class);

    private static final Logger LOG = LoggerFactory.getLogger(LazyBindingList.class);
    private static final String LAZY_CUTOFF_PROPERTY =
            "org.opendaylight.mdsal.binding.dom.codec.impl.LazyBindingList.max-eager-elements";
    private static final int DEFAULT_LAZY_CUTOFF = 16;

    @VisibleForTesting
    static final int LAZY_CUTOFF;

    static {
        final int value = Integer.getInteger(LAZY_CUTOFF_PROPERTY, DEFAULT_LAZY_CUTOFF);
        if (value < 0) {
            LOG.info("Lazy population of lists disabled");
            LAZY_CUTOFF = Integer.MAX_VALUE;
        } else {
            LOG.info("Using lazy population for lists larger than {} element(s)", value);
            LAZY_CUTOFF = value;
        }
    }

    private final ListNodeCodecContext<E> codec;
    private final Object[] objects;

    private LazyBindingList(final ListNodeCodecContext<E> codec,
            final Collection<? extends NormalizedNodeContainer<?, ?, ?>> entries) {
        this.codec = requireNonNull(codec);
        objects = entries.toArray();
    }

    static <E extends DataObject> @NonNull List<E> create(final ListNodeCodecContext<E> codec, final int size,
            final Collection<? extends NormalizedNodeContainer<?, ?, ?>> entries) {
        if (size == 1) {
            // Do not bother with lazy instantiation in case of a singleton
            return List.of(codec.createBindingProxy(entries.iterator().next()));
        }
        return size > LAZY_CUTOFF ? new LazyBindingList<>(codec, entries) : eagerList(codec, size, entries);
    }

    private static <E extends DataObject> @NonNull List<E> eagerList(final ListNodeCodecContext<E> codec,
            final int size, final Collection<? extends NormalizedNodeContainer<?, ?, ?>> entries) {
        @SuppressWarnings("unchecked")
        final E[] objs = (E[]) new DataObject[size];
        int offset = 0;
        for (NormalizedNodeContainer<?, ?, ?> node : entries) {
            objs[offset++] = codec.createBindingProxy(node);
        }
        verify(offset == objs.length);
        return List.of(objs);
    }

    @Override
    public int size() {
        return objects.length;
    }

    @Override
    public E get(final int index) {
        final Object obj = OBJ_AA.getAcquire(objects, index);
        // Check whether the object has been converted. The object is always non-null, but it can either be in DOM form
        // (either a MapEntryNode or UnkeyedListEntryNode) or in Binding form. We know the exact class for the latter,
        // as we are creating it via codec -- hence we can perform a direct comparison.
        //
        // We could do a Class.isInstance() check here, but since the implementation is not marked as final (yet) we
        // would be at the mercy of CHA being able to prove this invariant.
        return obj.getClass() == codec.generatedClass() ? (E) obj : load(index, (NormalizedNodeContainer<?, ?, ?>) obj);
    }

    private @NonNull E load(final int index, final NormalizedNodeContainer<?, ?, ?> node) {
        final E ret = codec.createBindingProxy(node);
        final Object witness;
        return (witness = OBJ_AA.compareAndExchangeRelease(objects, index, node, ret)) == node ? ret : (E) witness;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final Collection<? extends E> c) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean addAll(final int index, final Collection<? extends E> c) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw uoe();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void sort(final Comparator<? super E> c) {
        throw uoe();
    }

    @Override
    public void replaceAll(final UnaryOperator<E> operator) {
        throw uoe();
    }

    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Modification not supported");
    }
}
