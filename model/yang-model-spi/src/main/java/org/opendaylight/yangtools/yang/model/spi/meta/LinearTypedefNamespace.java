/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * A filter-based implementation of a Map to serve with {@link TypedefAwareEffectiveStatement#typedefNamespace()}.
 */
final class LinearTypedefNamespace extends AbstractMap<QName, TypedefEffectiveStatement> implements Immutable {
    private final Collection<TypedefEffectiveStatement> values;

    @SuppressWarnings("unchecked")
    LinearTypedefNamespace(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        values = (Collection<TypedefEffectiveStatement>)
            Collections2.filter(substatements, TypedefEffectiveStatement.class::isInstance);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(final Object value) {
        return values.contains(requireNonNull(value));
    }

    @Override
    public TypedefEffectiveStatement get(final Object key) {
        final var nonnull = requireNonNull(key);
        return values().stream().filter(stmt -> nonnull.equals(stmt.argument())).findFirst().orElse(null);
    }

    @Override
    public TypedefEffectiveStatement remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void putAll(final Map<? extends QName, ? extends TypedefEffectiveStatement> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QName> keySet() {
        return values.stream().map(TypedefEffectiveStatement::argument).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Collection<TypedefEffectiveStatement> values() {
        return values;
    }

    @Override
    public Set<Entry<QName, TypedefEffectiveStatement>> entrySet() {
        return values.stream().map(stmt -> Map.entry(stmt.argument(), stmt)).collect(ImmutableSet.toImmutableSet());
    }
}
