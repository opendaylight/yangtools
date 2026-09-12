/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * An {@link Externalizable} proxy for {@link HashAugmentations}.
 */
final class HAv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private HashAugmentations<?> augmentations;

    @SuppressWarnings("checkstyle:redundantModifier")
    public HAv1() {
        // For Externalizable
    }

    HAv1(final HashAugmentations<?> augmentations) {
        this.augmentations = requireNonNull(augmentations);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // clone to ensure values are not instantiated
        final var list = List.copyOf(augmentations.clone().values());
        out.writeInt(list.size());
        for (var augmentation : list) {
            out.writeObject(augmentation);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws ClassNotFoundException, IOException {
        augmentations = readExternal(in, in.readInt());
    }

    private static <C extends DataContainer & Augmentable<C>> @NonNull HashAugmentations<C> readExternal(
            final @NonNull ObjectInput in, final int size) throws ClassNotFoundException, IOException {
        if (size < 0) {
            throw new StreamCorruptedException("negative size");
        }

        final var map = new HashAugmentations<C>();
        for (int i = 0; i < size; ++i) {
            @SuppressWarnings("unchecked")
            final Augmentation<C, ?> augmentation = Augmentation.class.cast(in.readObject());
            map.set(augmentation);
        }
        final var mapSize = map.size();
        if (mapSize != size) {
            throw new StreamCorruptedException("mismatched size: expected " + size + ", actual " + mapSize);
        }
        return map;
    }

    @java.io.Serial
    Object readResolve() {
        return verifyNotNull(augmentations);
    }
}
