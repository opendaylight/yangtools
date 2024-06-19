/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.function.IntFunction;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class LeafSetNodeCodecContext extends ValueNodeCodecContext.WithCodec {
    private final IntFunction<ImmutableCollection.Builder<Object>> builderFactory;

    LeafSetNodeCodecContext(final LeafListSchemaNode schema, final ValueCodec<Object, Object> codec,
            final String getterName) {
        // FIXME: add support for defaults
        super(schema, codec, getterName, null);
        builderFactory = schema.isUserOrdered() ? ImmutableList::builderWithExpectedSize
            : ImmutableSet::builderWithExpectedSize;
    }

    @Override
    protected ImmutableCollection<?> deserializeObject(final NormalizedNode normalizedNode) {
        if (normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            final var domValues = ((LeafSetNode<Object>) normalizedNode).body();
            final var codec = getValueCodec();
            final var builder = builderFactory.apply(domValues.size());
            for (var valueNode : domValues) {
                builder.add(codec.deserialize(valueNode.body()));
            }
            return builder.build();
        }
        return null;
    }
}
