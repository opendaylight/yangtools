/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Combination of {@link BindingSerializer} and {@link BindingDeserializer}. This interface is present in this package
 * only due to constraints imposed by current implementation.
 */
// FIXME: this interface should not be necessary
interface BindingCodec<P, I> extends BindingSerializer<P, I>, BindingDeserializer<I, P>, Codec<P, I> {

    @Override
    P serialize(I input);

    @Override
    I deserialize(P input);

    I deserialize(P input, InstanceIdentifier<?> bindingIdentifier);

}
