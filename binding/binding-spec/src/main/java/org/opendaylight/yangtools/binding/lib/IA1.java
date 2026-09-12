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
import java.io.Serializable;
import org.opendaylight.yangtools.binding.Augmentation;

/**
 * A {@link Serializable} proxy for {@link ImmutableAugmentations1}.
 *
 * @since 16.0.1
 */
final class IA1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private Augmentation<?, ?> augmentation;

    @SuppressWarnings("checkstyle:redundantModifier")
    public IA1() {
        // For Externalizable
    }

    IA1(final Augmentation<?, ?> augmentation) {
        this.augmentation = requireNonNull(augmentation);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(augmentation);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final var local = Augmentation.class.cast(in.readObject());
        final var implementedInterface = local.implementedInterface().asSubclass(Augmentation.class);
        augmentation = implementedInterface.cast(local);
    }

    @java.io.Serial
    Object readResolve() {
        return ImmutableAugmentations.of(verifyNotNull(augmentation));
    }
}
