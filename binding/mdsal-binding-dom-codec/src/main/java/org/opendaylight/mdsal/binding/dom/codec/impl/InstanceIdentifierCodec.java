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
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class InstanceIdentifierCodec implements Codec<YangInstanceIdentifier, InstanceIdentifier<?>> {
    private final BindingCodecContext context;

    InstanceIdentifierCodec(final BindingCodecContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public YangInstanceIdentifier serialize(final InstanceIdentifier<?> input) {
        final List<PathArgument> domArgs = new ArrayList<>();
        context.getCodecContextNode(input, domArgs);
        return YangInstanceIdentifier.create(domArgs);
    }

    @Override
    public InstanceIdentifier<?> deserialize(final YangInstanceIdentifier input) {
        final List<InstanceIdentifier.PathArgument> builder = new ArrayList<>();
        final NodeCodecContext<?> codec = context.getCodecContextNode(input, builder);
        if (codec == null) {
            return null;
        }
        if (codec instanceof ListNodeCodecContext && Iterables.getLast(builder) instanceof InstanceIdentifier.Item) {
            // We ended up in list, but without key, which means it represent list as a whole,
            // which is not binding representable.
            return null;
        }
        return InstanceIdentifier.create(builder);
    }
}