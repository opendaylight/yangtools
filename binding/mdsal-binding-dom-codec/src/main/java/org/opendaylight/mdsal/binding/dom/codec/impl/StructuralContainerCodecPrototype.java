/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObjectStep;

/**
 * A prototype for a {@link StructuralContainerCodecContext}.
 */
final class StructuralContainerCodecPrototype extends ContainerLikeCodecPrototype {
    StructuralContainerCodecPrototype(final DataObjectStep<?> step, final ContainerRuntimeType container,
            final CodecContextFactory factory) {
        super(step, container, factory);
    }

    @Override
    StructuralContainerCodecContext<?> createInstance() {
        return new StructuralContainerCodecContext<>(this);
    }
}
