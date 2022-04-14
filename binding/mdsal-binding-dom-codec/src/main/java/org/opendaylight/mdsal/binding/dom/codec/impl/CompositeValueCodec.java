/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;

final class CompositeValueCodec extends AbstractIllegalArgumentCodec<Object, Object> {
    private final EncapsulatedValueCodec typeObjectCodec;
    @SuppressWarnings("rawtypes")
    // FIXME: specialize for the two possibilities
    private final IllegalArgumentCodec valueCodec;

    CompositeValueCodec(final Class<?> valueType, final IdentityCodec codec) {
        typeObjectCodec = EncapsulatedValueCodec.ofUnchecked(valueType);
        valueCodec = requireNonNull(codec);
    }

    CompositeValueCodec(final Class<?> valueType, final InstanceIdentifierCodec codec) {
        typeObjectCodec = EncapsulatedValueCodec.ofUnchecked(valueType);
        valueCodec = requireNonNull(codec);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object deserializeImpl(final Object input) {
        // FIXME: throws NPE on unrepresentable InstanceIdentifierCodec
        return typeObjectCodec.deserialize(valueCodec.deserialize(input));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object serializeImpl(final Object input) {
        return valueCodec.serialize(typeObjectCodec.serialize(input));
    }
}
