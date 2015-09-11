/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.util.MutableOffsetMap;
import org.opendaylight.yangtools.util.OffsetMapCache;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

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
            return ImmutableMap.of();
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
        final Collection<PathArgument> possibleKeys = new ArrayList<>(possibleChildren(schema));
        final Map<PathArgument, Integer> offsets = OffsetMapCache.offsetsFor(possibleKeys);
        return new ChildTrackingPolicy() {
            @Override
            Map<PathArgument, ModifiedNode> createMap() {
                return MutableOffsetMap.forOffsets(offsets);
            }
        };
    }

    // FIXME: is there a utility for this?
    private static Set<PathArgument> possibleChildren(final DataNodeContainer schema) {
        final Set<PathArgument> result = new HashSet<>();
        for (DataSchemaNode childSchema : schema.getChildNodes()) {
            if (childSchema instanceof ChoiceCaseNode) {
                result.addAll(possibleChildren(((DataNodeContainer) childSchema)));
            } else if (!(childSchema instanceof AugmentationSchema)) {
                result.add(NodeIdentifier.create(childSchema.getQName()));
            }
        }

        if (schema instanceof AugmentationTarget) {
            for (AugmentationSchema augmentationSchema : ((AugmentationTarget) schema).getAvailableAugmentations()) {
                result.add(SchemaUtils.getNodeIdentifierForAugmentation(augmentationSchema));
            }
        }

        return result;
    }
}
