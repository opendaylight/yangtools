/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * An {@link IdentifierNamespace} implementing a {@link QName} internment interface. Lookups in this namespace always
 * return a non-null object. They capture the first object instance and return that on subsequent lookups.
 */
public final class QNameCacheNamespace extends NamespaceBehaviour<QName, QName, QNameCacheNamespace>
    implements IdentifierNamespace<QName, QName> {

    private static final QNameCacheNamespace INSTANCE = new QNameCacheNamespace();

    private QNameCacheNamespace() {
        super(QNameCacheNamespace.class);
    }

    public static QNameCacheNamespace getInstance() {
        return INSTANCE;
    }

    @Override
    public QName get(@Nonnull final QName identifier) {
        throw new UnsupportedOperationException("Identifier/implementation API borkage");
    }

    private static NamespaceStorageNode getRoot(final NamespaceStorageNode storage) {
        NamespaceStorageNode wlk = storage;

        while (wlk.getParentNamespaceStorage() != null) {
            wlk = wlk.getParentNamespaceStorage();
        }

        return wlk;
    }

    @Override
    public QName getFrom(final NamespaceStorageNode storage, final QName key) {
        final NamespaceStorageNode root = getRoot(storage);
        final QName stored = root.getFromLocalStorage(QNameCacheNamespace.class, key);
        if (stored != null) {
            return stored;
        }

        root.putToLocalStorage(QNameCacheNamespace.class, key, key);
        return key;
    }

    @Override
    public Map<QName, QName> getAllFrom(final NamespaceStorageNode storage) {
        return getRoot(storage).getAllFromLocalStorage(QNameCacheNamespace.class);
    }

    @Override
    public void addTo(final NamespaceStorageNode storage, final QName key, final QName value) {
        throw new UnsupportedOperationException("Automagically populated");
    }
}
