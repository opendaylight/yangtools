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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.DataObjectWildcard;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements BindingInstanceIdentifierCodec,
        //FIXME: this is not really an IllegalArgumentCodec, as it can legally return null from deserialize()
        ValueCodec<YangInstanceIdentifier, DataObjectWildcard<?>> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public <T extends DataObject> DataObjectWildcard<T> toBinding(final YangInstanceIdentifier domPath) {
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

        return DataObjectWildcard.unsafeOf(builder);
    }

    @Override
    public @NonNull YangInstanceIdentifier fromBinding(final DataObjectWildcard<?> bindingPath) {
        final var domArgs = new ArrayList<PathArgument>();
        context.getCodecContextNode(bindingPath, domArgs);
        return YangInstanceIdentifier.of(domArgs);
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier serialize(final DataObjectWildcard<?> input) {
        return fromBinding(input);
    }

    @Override
    @Deprecated
    public DataObjectWildcard<?> deserialize(final YangInstanceIdentifier input) {
        return toBinding(input);
    }
}
