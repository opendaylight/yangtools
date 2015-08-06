/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class MapNodeModification implements Modification<ListSchemaNode, MapNode> {

    public static final MapEntryNodeModification MAP_ENTRY_NODE_MODIFICATION = new MapEntryNodeModification();

    @Override
    public Optional<MapNode> modify(ListSchemaNode schema, Optional<MapNode> actual,
                                    Optional<MapNode> modification, OperationStack operationStack) throws DataModificationException {

        // Merge or None operation on parent, leaving actual if modification not present
        if (!modification.isPresent()) {
            return actual;
        }

        Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> resultNodes = Maps.newLinkedHashMap();
        if(actual.isPresent()) {
            resultNodes.putAll(mapEntries(actual.get()));
        }

        // TODO implement ordering for modification nodes

        for (MapEntryNode mapEntryModification : modification.get().getValue()) {

            operationStack.enteringNode(mapEntryModification);

            YangInstanceIdentifier.NodeIdentifierWithPredicates entryKey = mapEntryModification.getIdentifier();

            switch (operationStack.getCurrentOperation()) {
            case NONE:
                DataModificationException.DataMissingException.check(schema.getQName(), actual, mapEntryModification);
            case MERGE: {
                MapEntryNode mergedListNode;
                if (resultNodes.containsKey(entryKey)) {
                    MapEntryNode actualEntry = resultNodes.get(entryKey);
                    mergedListNode = MAP_ENTRY_NODE_MODIFICATION.modify(schema, Optional.of(actualEntry), Optional.of(mapEntryModification), operationStack).get();
                } else {
                    mergedListNode = mapEntryModification;
                }

                resultNodes.put(mergedListNode.getIdentifier(), mergedListNode);
                break;
            }
            case CREATE: {
                DataModificationException.DataExistsException.check(schema.getQName(), actual, mapEntryModification);
            }
            case REPLACE: {
                resultNodes.put(entryKey, mapEntryModification);
                break;
            }
            case DELETE: {
                DataModificationException.DataMissingException.check(schema.getQName(), actual, mapEntryModification);
            }
            case REMOVE: {
                if (resultNodes.containsKey(entryKey)) {
                    resultNodes.remove(entryKey);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException(String.format("Unable to perform operation: %s on: %s, unknown", operationStack.getCurrentOperation(), schema));
            }

            operationStack.exitingNode(mapEntryModification);
        }
        return build(schema, resultNodes);
    }

    private Optional<MapNode> build(ListSchemaNode schema, Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> resultNodes) {
        if(resultNodes.isEmpty()) {
            return Optional.absent();
        }

        CollectionNodeBuilder<MapEntryNode, MapNode> b = Builders.mapBuilder(schema);

        for (MapEntryNode child : resultNodes.values()) {
            b.withChild(child);
        }

        return Optional.of(b.build());
    }

    private Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntries(MapNode mapNode) {
        Map<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapped = Maps.newLinkedHashMap();

        for (MapEntryNode mapEntryNode : mapNode.getValue()) {
            mapped.put(mapEntryNode.getIdentifier(), mapEntryNode);
        }

        return mapped;
    }

}
