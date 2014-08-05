/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.gen.impl;

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

abstract class AugmentableDataNodeContainerEmmiterSource extends DataNodeContainerSerializerSource {

    public AugmentableDataNodeContainerEmmiterSource(final AbstractStreamWriterGenerator generator, final GeneratedType type, final DataNodeContainer node) {
        super(generator, type, node);
        staticConstant(AUGMENTABLE_SERIALIZER, DataObjectSerializerImplementation.class, StreamWriterGenerator.AUGMENTABLE);
    }

    @Override
    protected void emitAfterBody(final StringBuilder b) {
        b.append(statement(invoke(AUGMENTABLE_SERIALIZER, "serialize", REGISTRY, INPUT, STREAM)));
    }
}