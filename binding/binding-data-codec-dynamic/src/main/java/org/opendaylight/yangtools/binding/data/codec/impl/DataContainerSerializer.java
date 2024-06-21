/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * A serializer which writes DataObject to supplied stream event writer.
 */
public final class DataContainerSerializer {
    private final @NonNull DataContainerSerializerRegistry registry;
    private final @NonNull DataContainerStreamer<?> delegate;

    DataContainerSerializer(final DataContainerSerializerRegistry registry, final DataContainerStreamer<?> delegate) {
        this.registry = requireNonNull(registry);
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Writes stream events representing object to supplied stream.
     *
     * @param obj Source of stream events
     * @param stream Stream to which events should be written.
     */
    void serialize(final DataContainer obj, final BindingStreamEventWriter stream) throws IOException {
        delegate.serialize(registry, obj, stream);
    }
}
