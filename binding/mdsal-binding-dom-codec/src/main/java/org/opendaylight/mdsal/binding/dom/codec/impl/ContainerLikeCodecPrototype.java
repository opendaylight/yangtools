/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A prototype for a {@link ContainerLikeCodecContext}.
 */
sealed class ContainerLikeCodecPrototype extends DataObjectCodecPrototype<ContainerLikeRuntimeType<?, ?>>
        permits StructuralContainerCodecPrototype {
    ContainerLikeCodecPrototype(final DataObjectStep<?> step, final ContainerLikeRuntimeType<?, ?> type,
            final CodecContextFactory factory) {
        super(step, NodeIdentifier.create(type.statement().argument()), type, factory);
    }

    @Override
    ContainerLikeCodecContext<?> createInstance() {
        return new ContainerLikeCodecContext<>(this);
    }
}
