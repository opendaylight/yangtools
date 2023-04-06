/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An {@link NamespaceBehaviour} which derives keys from a different namespace.
 *
 * @param <K> Key type
 * @param <V> Value type
 * @param <L> Original key type
 * @param <O> Original namespace type
 */
public abstract class DerivedNamespaceBehaviour<K, V, L, O extends ParserNamespace<L, ?>>
        extends NamespaceBehaviour<K, V> {

    private final @NonNull O derivedFrom;

    protected DerivedNamespaceBehaviour(final ParserNamespace<K, V> namespace, final O derivedFrom) {
        super(namespace);
        this.derivedFrom = requireNonNull(derivedFrom);
    }

    public final @NonNull O getDerivedFrom() {
        return derivedFrom;
    }

    @Override
    public Map<K, V> getAllFrom(final NamespaceStorage storage) {
        throw new UnsupportedOperationException("Virtual namespaces does not support provision of all items.");
    }

    @Override
    public abstract V getFrom(NamespaceStorage storage, K key);

    @Override
    public void addTo(final NamespaceStorage storage, final K key, final V value) {
        // Intentional noop
    }

    public abstract L getSignificantKey(K key);

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("derivedFrom", derivedFrom);
    }
}
