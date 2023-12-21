/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;

/**
 * {@link MapNode} which additionally preserves user-supplied ordering. This node represents a data instance of
 * a {@code list} with {@code ordered-by user;} substatement and a {@code key} definition.
 */
public non-sealed interface UserMapNode extends MapNode, OrderedNodeContainer<MapEntryNode> {
    @Override
    default Class<UserMapNode> contract() {
        return UserMapNode.class;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The implementation is required to define a user-visible iteration order, which must match {@link #childAt(int)}.
     */
    @Override
    Map<NodeIdentifierWithPredicates, MapEntryNode> asMap();

    /**
     * A builder of {@link SystemMapNode}s.
     */
    interface Builder extends CollectionNodeBuilder<MapEntryNode, UserMapNode> {
        /**
         * Return the resulting {@link UserMapNode}.
         *
         * @return resulting {@link UserMapNode}
         * @throws IllegalStateException if this builder does not have sufficient state
         */
        @NonNull UserMapNode build();
    }
}
