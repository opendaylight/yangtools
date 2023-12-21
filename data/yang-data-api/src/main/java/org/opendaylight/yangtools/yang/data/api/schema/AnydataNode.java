/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;

/**
 * A NormalizedNode holding the contents of an {@code anydata} node in some object model. While no guarantees are placed
 * on object models, there are related interfaces available for data interchange:
 *
 * <ul>
 *   <li>{@link NormalizedAnydata}, which exposes the contents as a {@link NormalizedNode} with attached schema
 *       information</li>
 *   <li>{@link NormalizableAnydata}, which is trait optionally implemented by object models and allows the opaque,
 *       implementation-specific representation to be interpreted in a the context of provided schema information,
 *       potentially forming a NormalizedAnydata node.
 * </ul>
 *
 * @param <V> Value type, uniquely identifying the object model used for values
 */
@Beta
public non-sealed interface AnydataNode<V> extends ForeignDataNode<V> {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<AnydataNode> contract() {
        return AnydataNode.class;
    }

    /**
     * A builder of {@link AnydataNode}s.
     */
    interface Builder<V> extends NormalizedNodeBuilder<NodeIdentifier, V, AnydataNode<V>> {
        // Just a specialization
    }
}
