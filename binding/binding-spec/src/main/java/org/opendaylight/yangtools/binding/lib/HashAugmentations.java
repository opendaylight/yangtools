/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * {@link MutableAugmentations} implemented in terms of a {@link HashMap}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.0.1
 */
final class HashAugmentations<C extends DataContainer & Augmentable<C>>
        extends HashMap<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> implements MutableAugmentations<C> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    HashAugmentations() {
        // TODO: pre-sizing policy?
    }

    HashAugmentations(final Map<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> map) {
        super(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashAugmentations<C> clone() {
        return (HashAugmentations<C>) super.clone();
    }

    @java.io.Serial
    private Object writeReplace() {
        return new HAv1(this);
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw nse();
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void readObjectNoData() throws ObjectStreamException {
        throw nse();
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throw nse();
    }

    private static NotSerializableException nse() {
        return new NotSerializableException(HashAugmentations.class.getName());
    }
}
