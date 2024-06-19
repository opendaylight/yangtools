/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path
 * element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public class KeyedInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>> extends InstanceIdentifier<T> {
    private static final long serialVersionUID = 1L;
    private final K key;

    KeyedInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> pathArguments, final boolean wildcarded, final int hash, final K key) {
        super(type, pathArguments, wildcarded, hash);
        this.key = key;
    }

    /**
     * Return the key attached to this identifier. This method is equivalent to
     * calling {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
     *
     * @return Key associated with this instance identifier.
     */
    public final K getKey() {
        return key;
    }

    @Override
    public final InstanceIdentifierBuilder<T> builder() {
        return new InstanceIdentifierBuilderImpl<T>(new InstanceIdentifier.IdentifiableItem<T, K>(getTargetType(), key), pathArguments, hashCode(), isWildcarded());
    }

    @Override
    protected boolean fastNonEqual(final InstanceIdentifier<?> other) {
        final KeyedInstanceIdentifier<?, ?> kii = (KeyedInstanceIdentifier<?, ?>) other;

        /*
         * We could do an equals() here, but that may actually be expensive.
         * equals() in superclass falls back to a full compare, which will
         * end up running that equals anyway, so do not bother here.
         */
        return (key == null) != (kii.key == null);
    }
}
