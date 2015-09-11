/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.yangtools.util.MutableOffsetMap;
import org.opendaylight.yangtools.util.MutableOffsetMapFactory;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

/**
 * Child ordering policy. It defines how a {@link ModifiedNode} tracks its children.
 */
abstract class ChildTrackingPolicy {
    private static final int DEFAULT_CHILD_COUNT = 8;

    /**
     * No child nodes are possible, ever.
     */
    static final ChildTrackingPolicy NONE = new ChildTrackingPolicy() {
        @Override
        Map<PathArgument, ModifiedNode> createMap() {
            return Collections.emptyMap();
        }
    };
    /**
     * Child nodes are possible and we need to make sure that their iteration order
     * matches the order in which they are introduced.
     */
    static final ChildTrackingPolicy ORDERED = new ChildTrackingPolicy() {
        @Override
        Map<PathArgument, ModifiedNode> createMap() {
            return new LinkedHashMap<>(DEFAULT_CHILD_COUNT);
        }
    };
    /**
     * Child nodes are possible, but their iteration order can be undefined.
     */
    static final ChildTrackingPolicy UNORDERED = new ChildTrackingPolicy() {
        @Override
        Map<PathArgument, ModifiedNode> createMap() {
            return new HashMap<>();
        }
    };

    /**
     * Instantiate a new map for all possible children.
     *
     * @return An empty map instance
     */
    abstract Map<PathArgument, ModifiedNode> createMap();

    /**
     * Create a {@link ChildTrackingPolicy}, which will instantiate {@link MutableOffsetMap} for tracking children with
     * of a {@link DataNodeContainer}.
     *
     * @param childNodes Set of possible children
     * @return A ChildTracingPolicy instance
     */
    static ChildTrackingPolicy forSchema(final DataNodeContainer schema) {
        final MutableOffsetMapFactory<PathArgument> factory = MutableOffsetMapFactory.unordered(
            SchemaUtils.possibleChildIdentifiers(schema));
        return new ChildTrackingPolicy() {
            @Override
            Map<PathArgument, ModifiedNode> createMap() {
                return factory.newEmptyMap();
            }
        };
    }
}
