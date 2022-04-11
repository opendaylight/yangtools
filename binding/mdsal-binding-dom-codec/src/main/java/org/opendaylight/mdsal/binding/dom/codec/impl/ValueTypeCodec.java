/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Value codec, which serializes / deserializes values from DOM simple values.
 */
// FIXME: IllegalArgumentCodec is perhaps not appropriate here due to null behavior
abstract class ValueTypeCodec implements IllegalArgumentCodec<Object, Object> {

    @SuppressWarnings("rawtypes")
    static ValueTypeCodec encapsulatedValueCodecFor(final Class<?> typeClz, final TypeDefinition<?> typeDef,
             final IllegalArgumentCodec delegate) {
        return new CompositeValueCodec(SchemaUnawareCodec.of(typeClz, typeDef), delegate);
    }
}
