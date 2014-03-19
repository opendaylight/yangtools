/*
* Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*/
package org.opendaylight.yangtools.yang.data.operations;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class MapNodeModification implements Modification<ListSchemaNode, MapNode> {

    public static final ContainerNodeModification CONTAINER_NODE_MODIFICATION = new ContainerNodeModification();

    @Override
    public Optional<MapNode> modify(ListSchemaNode schemaNode, Optional<MapNode> actual,
                                    Optional<MapNode> modification, OperationStack operationStack) throws DataModificationException {

        // Merge or None operation on parent, leaving actual if modification not present
        if (modification.isPresent() == false)
            return actual;

        Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> resultNodes = Maps.newLinkedHashMap();
        if(actual.isPresent())
            resultNodes.putAll(mapEntries(actual.get()));

        // TODO implement ordering for modification nodes

        for (MapEntryNode mapEntryModification : modification.get().getValue()) {

            operationStack.enteringNode(mapEntryModification);

            InstanceIdentifier.NodeIdentifierWithPredicates entryKey = mapEntryModification.getIdentifier();

            switch (operationStack.getCurrentOperation()) {
            case NONE:
                DataModificationException.DataMissingException.check(schemaNode.getQName(), actual, mapEntryModification);
            case MERGE: {
                MapEntryNode mergedListNode;
                if (resultNodes.containsKey(entryKey)) {
                    MapEntryNode actualEntry = resultNodes.get(entryKey);
                    mergedListNode = new MapEntryNodeModification().modify(schemaNode, Optional.of(actualEntry), Optional.of(mapEntryModification), operationStack).get();
                } else {
                    mergedListNode = mapEntryModification;
                }

                resultNodes.put(mergedListNode.getIdentifier(), mergedListNode);
                break;
            }
            case CREATE: {
                DataModificationException.DataExistsException.check(schemaNode.getQName(), actual, mapEntryModification);
            }
            case REPLACE: {
                resultNodes.put(entryKey, mapEntryModification);
                break;
            }
            case DELETE: {
                DataModificationException.DataMissingException.check(schemaNode.getQName(), actual, mapEntryModification);
            }
            case REMOVE: {
                if (resultNodes.containsKey(entryKey)) {
                    resultNodes.remove(entryKey);
                }
                break;
            }
            default:
                throw new IllegalStateException("Unable to perform operation " + operationStack.getCurrentOperation());
            }

            operationStack.exitingNode(mapEntryModification);
        }
        return build(schemaNode, resultNodes);
    }

    private Optional<MapNode> build(ListSchemaNode schemaNode, Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> resultNodes) {
        if(resultNodes.isEmpty())
            return Optional.absent();

        CollectionNodeBuilder<MapEntryNode, MapNode> b = Builders.mapBuilder(schemaNode);

        for (MapEntryNode child : resultNodes.values()) {
            b.withChild(child);
        }

        return Optional.of(b.build());
    }

    private Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapEntries(MapNode mapNode) {
        Map<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> mapped = Maps.newHashMap();

        for (MapEntryNode mapEntryNode : mapNode.getValue()) {
            mapped.put(mapEntryNode.getIdentifier(), mapEntryNode);
        }

        return mapped;
    }

}
