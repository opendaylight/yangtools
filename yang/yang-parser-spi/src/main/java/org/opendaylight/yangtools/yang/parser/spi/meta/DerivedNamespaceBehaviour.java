/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

@SuppressWarnings("checkstyle:classTypeParameterName")
public abstract class DerivedNamespaceBehaviour<K, V, DK, N extends IdentifierNamespace<K, V>,
       DN extends IdentifierNamespace<DK, ?>> extends NamespaceBehaviour<K, V, N> {

    private final Class<DN> derivedFrom;

    protected DerivedNamespaceBehaviour(Class<N> identifier, Class<DN> derivedFrom) {
        super(identifier);
        this.derivedFrom = Preconditions.checkNotNull(derivedFrom);
    }

    public Class<DN> getDerivedFrom() {
        return derivedFrom;
    }

    @Override
    public Map<K, V> getAllFrom(NamespaceStorageNode storage) {
        throw new UnsupportedOperationException("Virtual namespaces does not support provision of all items.");
    }

    @Override
    public abstract V getFrom(NamespaceBehaviour.NamespaceStorageNode storage, K key);

    @Override
    public void addTo(NamespaceStorageNode storage, K key, V value) {
        // Intentional noop
    }

    public abstract DK getSignificantKey(K key);
}
