/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;

abstract class AbstractBindingCodecTest extends AbstractBindingRuntimeTest {
    BindingCodecContext codecContext;

    @BeforeEach
    final void setupCodecContext() {
        codecContext = new BindingCodecContext(getRuntimeContext());
    }

    @SuppressWarnings("unchecked")
    protected final <T extends DataObject> T thereAndBackAgain(final DataObjectReference<T> path, final T data) {
        final var there = codecContext.toNormalizedDataObject(path, data);
        final var backAgain = codecContext.fromNormalizedNode(there.path(), there.node());
        assertEquals(path, backAgain.getKey());
        return (T) backAgain.getValue();
    }
}
