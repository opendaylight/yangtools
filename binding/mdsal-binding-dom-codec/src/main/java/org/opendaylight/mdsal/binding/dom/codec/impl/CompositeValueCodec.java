/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;

final class CompositeValueCodec extends ValueTypeCodec {
    private final SchemaUnawareCodec bindingToSimpleType;
    @SuppressWarnings("rawtypes")
    // FIXME: this is probably not right w.r.t. null
    private final IllegalArgumentCodec bindingToDom;

    CompositeValueCodec(final SchemaUnawareCodec extractor,
            @SuppressWarnings("rawtypes") final IllegalArgumentCodec delegate) {
        this.bindingToSimpleType = extractor;
        this.bindingToDom = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(final Object input) {
        return bindingToSimpleType.deserialize(bindingToDom.deserialize(input));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object serialize(final Object input) {
        return bindingToDom.serialize(bindingToSimpleType.serialize(input));
    }
}
