/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.KeyAware;

/**
 * A prototype for a {@link MapCodecContext}.
 */
final class MapCodecPrototype extends ListCodecPrototype {
    MapCodecPrototype(final Item<?> item, final ListRuntimeType type, final CodecContextFactory factory) {
        super(item, type, factory);
        final var clazz = javaClass();
        checkArgument(KeyAware.class.isAssignableFrom(clazz), "%s is not KeyAware", clazz);
    }

    @Override
    ListCodecContext<?> createInstance() {
        return MapCodecContext.of(this);
    }
}
