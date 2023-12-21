/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;

/**
 * {@link MapNode} which additionally preserves user-supplied ordering. This node represents a data instance of
 * a {@code list} with {@code ordered-by user;} substatement and a {@code key} definition.
 */
public non-sealed interface SystemMapNode extends MapNode, OrderingAware.System {
    @Override
    default Class<SystemMapNode> contract() {
        return SystemMapNode.class;
    }

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

    /**
     * A builder of {@link SystemMapNode}s.
     */
    interface Builder extends CollectionNodeBuilder<MapEntryNode, SystemMapNode> {
        /**
         * Return the resulting {@link SystemMapNode}.
         *
         * @return resulting {@link SystemMapNode}
         * @throws IllegalStateException if this builder does not have sufficient state
         */
        @NonNull SystemMapNode build();
    }
}
