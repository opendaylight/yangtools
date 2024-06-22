/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements BindingInstanceIdentifierCodec,
        //FIXME: this is not really an IllegalArgumentCodec, as it can legally return null from deserialize()
        ValueCodec<YangInstanceIdentifier, DataObjectReference<?>> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public BindingInstanceIdentifier dataToBinding(final YangInstanceIdentifier domPath) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public DataObjectReference<?> toBinding(final YangInstanceIdentifier domPath) {
        final var builder = new ArrayList<ExactDataObjectStep<?>>();
        final var codec = context.getCodecContextNode(domPath, builder);
        if (codec == null) {
            return null;
        }
        if (codec instanceof ListCodecContext && builder.getLast() instanceof NodeStep) {
            // We ended up in list, but without key, which means it represent list as a whole,
            // which is not binding representable.
            return null;
        }

        return DataObjectReference.of(builder);
    }

    @Override
    public @NonNull YangInstanceIdentifier fromBinding(final @NonNull BindingInstanceIdentifier bindingPath) {
        return switch (bindingPath) {
            case DataObjectReference<?> ref -> {
                final var domArgs = new ArrayList<PathArgument>();
                context.getCodecContextNode(ref, domArgs);
                yield YangInstanceIdentifier.of(domArgs);
            }
        };
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier serialize(final DataObjectReference<?> input) {
        return fromBinding(input);
    }

    @Override
    @Deprecated
    public DataObjectReference<?> deserialize(final YangInstanceIdentifier input) {
        return switch (dataToBinding(input)) {
            case DataObjectReference<?> ref -> ref;
        };
    }
}
