/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;

/**
 * Containment node, which contains {@link MapEntryNode} of the same type, which may be quickly retrieved using a key.
 *
 * <p>
 * This node maps to the list node in YANG schema, schema and semantics of this node, its children and key construction
 * is defined by YANG {@code list} statement and its {@code key} and {@code ordered-by} substatements.
 */
public sealed interface MapNode
        extends DistinctNodeContainer<NodeIdentifierWithPredicates, MapEntryNode>, DataContainerChild, MixinNode
        permits SystemMapNode, UserMapNode {
    @Override
    Class<? extends MapNode> contract();

    /**
     * Return a {@link Map} view of this node. Note that the iteration order of the returned is map is not defined in
     * this interface.
     *
     * @return Map view of this node.
     */
    @Beta
    @NonNull Map<NodeIdentifierWithPredicates, MapEntryNode> asMap();

    @Override
    default Collection<@NonNull MapEntryNode> body() {
        return asMap().values();
    }

    @Override
    default int size() {
        return asMap().size();
    }

    @Override
    default boolean isEmpty() {
        return asMap().isEmpty();
    }

    /**
     * A builder of {@link MapNode}s.
     */
    sealed interface Builder<T extends MapNode> extends CollectionNodeBuilder<MapEntryNode, T>
        permits SystemMapNode.Builder, UserMapNode.Builder {
        // Just a specialization
    }
}
