/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class LeafSetNodeCodecContext extends ValueNodeCodecContext.WithCodec {
    LeafSetNodeCodecContext(final LeafListSchemaNode schema, final Codec<Object, Object> codec,
        final String getterName) {
        // FIXME: add support for defaults
        super(schema, codec, getterName, null);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        if (normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            final Collection<LeafSetEntryNode<Object>> domValues = ((LeafSetNode<Object>) normalizedNode).getValue();
            final List<Object> result = new ArrayList<>(domValues.size());
            for (final LeafSetEntryNode<Object> valueNode : domValues) {
                result.add(getValueCodec().deserialize(valueNode.getValue()));
            }
            return result;
        }
        return null;
    }
}
