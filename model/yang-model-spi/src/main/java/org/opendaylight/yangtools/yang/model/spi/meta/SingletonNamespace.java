/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespacedEffectiveStatement;

final class SingletonNamespace<T extends NamespacedEffectiveStatement<?>> implements Map<QName, T> {
    private final @NonNull T item;

    SingletonNamespace(final T item) {
        this.item = requireNonNull(item);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(final Object key) {
        return key.equals(item.argument());
    }

    @Override
    public boolean containsValue(final Object value) {
        return value.equals(item);
    }

    @Override
    public T get(final Object key) {
        return containsKey(key) ? item : null;
    }

    @Override
    public T put(final QName key, final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends QName, ? extends T> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QName> keySet() {
        return Set.of(item.argument());
    }

    @Override
    public Collection<T> values() {
        return List.of(item);
    }

    @Override
    public Set<Entry<QName, T>> entrySet() {
        return Set.of(Map.entry(item.argument(), item));
    }

    @Override
    public int hashCode() {
        return item.getIdentifier().hashCode() ^ item.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SingletonNamespace) {
            return item.equals(((SingletonNamespace<?>) obj).item);
        }
        if (obj instanceof Map) {
            final var it = ((Map<?, ?>) obj).entrySet().iterator();
            if (it.hasNext()) {
                final var entry = it.next();
                if (!it.hasNext() && item.argument().equals(entry.getKey()) && item.equals(entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "{" + item.argument() + "=" + item + "}";
    }
}
