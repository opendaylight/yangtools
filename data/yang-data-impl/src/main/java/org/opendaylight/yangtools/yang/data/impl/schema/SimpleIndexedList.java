/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.IndexedList;

/**
 * A naive implementation if {@link IndexedList}. This implementation seeks to be correct, but is not terribly efficient
 * nor thread-safe. Attempts at concurrent modification will result in a wrecked, unusable state.
 */
@Beta
public final class SimpleIndexedList<I, E extends Identifiable<I>> extends AbstractList<E>
        implements IndexedList<I, E> {
    private final List<E> elements = new ArrayList<>();
    private final Map<I, E> map = new HashMap<>();

    @Override
    public E lookupElement(final I id) {
        return map.get(requireNonNull(id));
    }

    @Override
    public int insertAfter(final I id, final E element) {
        final int idx = elementOffset(id) + 1;
        add(idx, element);
        return idx;
    }

    @Override
    public int insertBefore(final I id, final E element) {
        final int idx = elementOffset(id);
        add(idx, element);
        return idx;
    }

    @Override
    public E get(final int index) {
        return elements.get(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public void add(final int index, final E element) {
        final var id = element.getIdentifier();
        final var existing = map.putIfAbsent(id, element);
        checkArgument(existing == null, "Element %s would displace element %s", element, existing);
        elements.add(index, element);
    }

    @Override
    public E remove(final int index) {
        final var ret = elements.remove(index);
        verifyNotNull(map.remove(ret.getIdentifier(), ret));
        return ret;
    }

    private int elementOffset(final I id) {
        final var elem = map.get(requireNonNull(id));
        if (elem == null) {
            throw new NoSuchElementException("No element matching " + id);
        }
        final int index = elements.indexOf(elem);
        verify(index >= 0);
        return index;
    }
}
