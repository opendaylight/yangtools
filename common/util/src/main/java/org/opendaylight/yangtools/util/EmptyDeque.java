/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A specialized always-empty implementation of {@link java.util.Deque}. This implementation will always refuse new
 * elements in its {@link #offer(Object)} method.
 *
 * @param <E> the type of elements held in this collection
 * @deprecated This class is deprecated and scheduled for removal in the next major release.
 */
@Deprecated(since = "16.0.0", forRemoval = true)
public final class EmptyDeque<E> extends AbstractQueue<E> implements Deque<E>, Immutable {
    private static final EmptyDeque<?> INSTANCE = new EmptyDeque<>();
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private EmptyDeque() {
        // No instances
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @SuppressWarnings("unchecked")
    public static <T> EmptyDeque<T> instance() {
        return (EmptyDeque<T>) INSTANCE;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean offer(final E e) {
        return false;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean offerFirst(final E e) {
        return false;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean offerLast(final E e) {
        return false;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E poll() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E pollFirst() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E pollLast() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E peek() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E peekFirst() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E peekLast() {
        return null;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public int size() {
        return 0;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public Object[] toArray() {
        return EMPTY_ARRAY;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public <T> T[] toArray(final T[] a) {
        return requireNonNull(a);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void addFirst(final E e) {
        add(e);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void addLast(final E e) {
        add(e);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E removeFirst() {
        return remove();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E removeLast() {
        return remove();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E getFirst() {
        return element();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E getLast() {
        return element();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeFirstOccurrence(final Object o) {
        return false;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeLastOccurrence(final Object o) {
        return false;
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void push(final E e) {
        addFirst(e);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public E pop() {
        return removeFirst();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public Iterator<E> descendingIterator() {
        return Collections.emptyIterator();
    }
}
