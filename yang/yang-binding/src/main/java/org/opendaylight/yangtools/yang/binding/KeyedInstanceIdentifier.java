/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

public class KeyedInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>> extends InstanceIdentifier<T> {
    private final K key;

    KeyedInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> pathArguments, final boolean wildcarded, final int hash, final K key) {
        super(type, pathArguments, wildcarded, hash);
        this.key = key;
    }

    public final K getKey() {
        return key;
    }

    @Override
    public final InstanceIdentifierBuilder<T> builder() {
        return new InstanceIdentifierBuilderImpl<T>(new InstanceIdentifier.IdentifiableItem<T, K>(getTargetType(), key), getPathArguments(), hashCode(), isWildcarded());
    }
}
