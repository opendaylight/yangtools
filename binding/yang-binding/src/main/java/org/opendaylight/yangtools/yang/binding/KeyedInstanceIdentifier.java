/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.ObjectStreamException;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public class KeyedInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>>
        extends InstanceIdentifier<T> {
    @Serial
    private static final long serialVersionUID = 2L;

    private final K key;

    KeyedInstanceIdentifier(final Class<@NonNull T> type, final Iterable<PathArgument> pathArguments,
            final boolean wildcarded, final int hash, final K key) {
        super(type, pathArguments, wildcarded, hash);
        this.key = key;
    }

    /**
     * Return the key attached to this identifier. This method is equivalent to calling
     * {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
     *
     * @return Key associated with this instance identifier.
     */
    public final K getKey() {
        return key;
    }

    @Override
    public final KeyedBuilder<T, K> builder() {
        return new KeyedBuilder<>(this);
    }

    @Override
    protected boolean fastNonEqual(final InstanceIdentifier<?> other) {
        final KeyedInstanceIdentifier<?, ?> kii = (KeyedInstanceIdentifier<?, ?>) other;

        /*
         * We could do an equals() here, but that may actually be expensive.
         * equals() in superclass falls back to a full compare, which will
         * end up running that equals anyway, so do not bother here.
         */
        return key == null != (kii.key == null);
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new KeyedInstanceIdentifierV2<>(this);
    }
}
