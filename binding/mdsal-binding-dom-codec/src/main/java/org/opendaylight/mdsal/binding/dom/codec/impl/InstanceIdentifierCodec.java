/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements BindingInstanceIdentifierCodec,
        //FIXME: this is not really an IllegalArgumentCodec, as it can legally return null from deserialize()
        IllegalArgumentCodec<YangInstanceIdentifier, InstanceIdentifier<?>> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public <T extends DataObject> InstanceIdentifier<T> toBinding(final YangInstanceIdentifier domPath) {
        final List<InstanceIdentifier.PathArgument> builder = new ArrayList<>();
        final BindingDataObjectCodecTreeNode<?> codec = context.getCodecContextNode(domPath, builder);
        if (codec == null) {
            return null;
        }
        if (codec instanceof ListNodeCodecContext && Iterables.getLast(builder) instanceof InstanceIdentifier.Item) {
            // We ended up in list, but without key, which means it represent list as a whole,
            // which is not binding representable.
            return null;
        }
        @SuppressWarnings("unchecked")
        final InstanceIdentifier<T> ret = (InstanceIdentifier<T>) InstanceIdentifier.create(builder);
        return ret;
    }

    @Override
    public @NonNull YangInstanceIdentifier fromBinding(@NonNull final InstanceIdentifier<?> bindingPath) {
        final List<PathArgument> domArgs = new ArrayList<>();
        context.getCodecContextNode(bindingPath, domArgs);
        return YangInstanceIdentifier.create(domArgs);
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier serialize(final InstanceIdentifier<?> input) {
        return fromBinding(input);
    }

    @Override
    @Deprecated
    public InstanceIdentifier<?> deserialize(final YangInstanceIdentifier input) {
        return toBinding(input);
    }
}
