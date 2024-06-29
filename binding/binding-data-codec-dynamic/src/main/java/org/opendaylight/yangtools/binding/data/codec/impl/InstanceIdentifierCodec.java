/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements BindingInstanceIdentifierCodec,
        //FIXME: this is not really an IllegalArgumentCodec, as it can legally return null from deserialize()
        ValueCodec<YangInstanceIdentifier, BindingInstanceIdentifier> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Addressable> DataObjectReference<T> toBinding(final YangInstanceIdentifier domPath) {
        final var builder = new ArrayList<DataObjectStep<?>>();
        final var codec = context.getCodecContextNode(domPath, builder);
        if (codec == null) {
            return null;
        }
        if (codec instanceof ListCodecContext && Iterables.getLast(builder) instanceof KeylessStep) {
            // We ended up in list, but without key, which means it represent list as a whole,
            // which is not binding representable.
            return null;
        }

        return (DataObjectReference<T>) DataObjectReference.ofUnsafeSteps(builder);
    }

    @Override
    public YangInstanceIdentifier fromBinding(final DataObjectReference<?> bindingPath) {
        final var domArgs = new ArrayList<PathArgument>();
        context.getCodecContextNode(bindingPath, domArgs);
        return YangInstanceIdentifier.of(domArgs);
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier serialize(final BindingInstanceIdentifier input) {
        return fromBinding(input);
    }

    @Override
    @Deprecated
    public BindingInstanceIdentifier deserialize(final YangInstanceIdentifier input) {
        // FIXME: YANGTOOLS-1577: do not defer to InstanceIdentifier here
        final var binding = toBinding(input);
        if (binding == null) {
            throw new IllegalArgumentException(input + " cannot be represented as a BindingInstanceIdentifier");
        }
        try {
            return binding.toIdentifier();
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
