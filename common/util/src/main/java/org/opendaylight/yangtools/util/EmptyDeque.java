/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
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

 * @author Robert Varga
 *
 * @param <E> the type of elements held in this collection
 */
@Beta
public final class EmptyDeque<E> extends AbstractQueue<E> implements Deque<E>, Immutable {
    private static final EmptyDeque<?> INSTANCE = new EmptyDeque<>();
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private EmptyDeque() {
        // No instances
    }

    @SuppressWarnings("unchecked")
    public static <T> EmptyDeque<T> instance() {
        return (EmptyDeque<T>) INSTANCE;
    }

    @Override
    public boolean offer(final E entry) {
        return false;
    }

    @Override
    public boolean offerFirst(final E e) {
        return false;
    }

    @Override
    public boolean offerLast(final E e) {
        return false;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E pollFirst() {
        return null;
    }

    @Override
    public E pollLast() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public E peekFirst() {
        return null;
    }

    @Override
    public E peekLast() {
        return null;
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Object[] toArray() {
        return EMPTY_ARRAY;
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return Preconditions.checkNotNull(a);
    }

    @Override
    public void addFirst(final E e) {
        add(e);
    }

    @Override
    public void addLast(final E e) {
        add(e);
    }

    @Override
    public E removeFirst() {
        return remove();
    }

    @Override
    public E removeLast() {
        return remove();
    }

    @Override
    public E getFirst() {
        return element();
    }

    @Override
    public E getLast() {
        return element();
    }

    @Override
    public boolean removeFirstOccurrence(final Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(final Object o) {
        return false;
    }

    @Override
    public void push(final E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return Collections.emptyIterator();
    }
}
