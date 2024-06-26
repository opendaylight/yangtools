/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;

/**
 * A prototype for a {@link MapCodecContext}.
 */
final class MapCodecPrototype extends ListCodecPrototype {
    MapCodecPrototype(final DataObjectStep<?> step, final ListRuntimeType type, final CodecContextFactory factory) {
        super(step, type, factory);
        final var clazz = javaClass();
        checkArgument(KeyAware.class.isAssignableFrom(clazz), "%s is not KeyAware", clazz);
    }

    @Override
    ListCodecContext<?> createInstance() {
        return MapCodecContext.of(this);
    }
}
