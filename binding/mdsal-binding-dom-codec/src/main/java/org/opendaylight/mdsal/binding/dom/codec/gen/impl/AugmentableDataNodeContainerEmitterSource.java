/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

@Deprecated
abstract class AugmentableDataNodeContainerEmitterSource extends DataNodeContainerSerializerSource {
    private static final String AUGMENTABLE_SERIALIZER = "AUGMENTABLE_SERIALIZER";

    AugmentableDataNodeContainerEmitterSource(final AbstractStreamWriterGenerator generator, final GeneratedType type,
            final DataNodeContainer node) {
        super(generator, type, node);
        /*
         * Eventhough intuition says the serializer could reference the generator directly, that is not true in OSGi
         * environment -- so we need to resolve the reference first and inject it as a static constant.
         */
        staticConstant(AUGMENTABLE_SERIALIZER, DataObjectSerializerImplementation.class,
            StreamWriterGenerator.AUGMENTABLE);
    }

    @Override
    protected void emitAfterBody(final StringBuilder sb) {
        sb.append(statement(invoke(AUGMENTABLE_SERIALIZER, "serialize", REGISTRY, INPUT, STREAM)));
    }
}