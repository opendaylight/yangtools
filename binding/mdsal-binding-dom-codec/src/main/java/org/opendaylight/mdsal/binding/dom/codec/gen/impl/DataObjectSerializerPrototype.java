/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;

/**
 * Prototype of a DataObjectSerializerImplementation. This is a template class, which the
 * {@link AbstractStreamWriterGenerator} uses to instantiate {@link DataObjectSerializerImplementation} on a per-type
 * basis. During that time, the {@link #serialize(DataObjectSerializerRegistry, DataObject, BindingStreamEventWriter)}
 * method will be replaced by the real implementation.
 */
@Deprecated
final class DataObjectSerializerPrototype implements DataObjectSerializerImplementation {
    private static final DataObjectSerializerPrototype INSTANCE = new DataObjectSerializerPrototype();

    private DataObjectSerializerPrototype() {
        // Intentionally hidden, subclasses can replace it
    }

    /**
     * Return the shared serializer instance.
     *
     * @return Global singleton instance.
     */
    public static DataObjectSerializerPrototype getInstance() {
        return INSTANCE;
    }

    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj,
            final BindingStreamEventWriter stream) {
        throw new UnsupportedOperationException("Prototype body, this code should never be invoked.");
    }
}
