/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class UnkeyedListNodeModification implements Modification<ListSchemaNode, UnkeyedListNode> {

    public static final MapEntryNodeModification MAP_ENTRY_NODE_MODIFICATION = new MapEntryNodeModification();

    @Override
    public Optional<UnkeyedListNode> modify(ListSchemaNode schema, Optional<UnkeyedListNode> actual,
            Optional<UnkeyedListNode> modification, OperationStack operationStack) throws DataModificationException {

        // Merge or None operation on parent, leaving actual if modification not present
        if (!modification.isPresent()) {
            return actual;
        }

        List<UnkeyedListEntryNode> resultNodes = Lists.newArrayList();
        if (actual.isPresent())
            resultNodes = unkeyedListEntries(actual.get());

        // TODO implement ordering for modification nodes

        for (UnkeyedListEntryNode unkeyedListEntryModification : modification.get().getValue()) {

            operationStack.enteringNode(unkeyedListEntryModification);

            YangInstanceIdentifier.NodeIdentifier entryKey = unkeyedListEntryModification.getIdentifier();

            switch (operationStack.getCurrentOperation()) {
            case NONE:
                break;
            // DataModificationException.DataMissingException.check(schema.getQName(), actual, mapEntryModification);
            case MERGE:
            case CREATE: {
                DataModificationException.DataExistsException.check(schema.getQName(), actual,
                        unkeyedListEntryModification);
                resultNodes.add(unkeyedListEntryModification);
            }
            case REPLACE: {
                break;
            }
            case DELETE: {
                // DataModificationException.DataMissingException.check(schema.getQName(), actual,
                // unkeyedListEntryModification);
                break;
            }
            case REMOVE: {
                break;
            }
            default:
                throw new UnsupportedOperationException(
                        String.format("Unable to perform operation: %s on: %s, unknown",
                                operationStack.getCurrentOperation(), schema));
            }

            operationStack.exitingNode(unkeyedListEntryModification);
        }
        return build(schema, resultNodes);
    }

    private Optional<UnkeyedListNode> build(ListSchemaNode schema, List<UnkeyedListEntryNode> resultNodes) {
        if (resultNodes.isEmpty()) {
            return Optional.absent();
        }

        CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> b = Builders.unkeyedListBuilder();
        b.withNodeIdentifier(new NodeIdentifier(schema.getQName()));

        for (UnkeyedListEntryNode child : resultNodes) {
            b.withChild(child);
        }

        return Optional.of(b.build());
    }

    private List<UnkeyedListEntryNode> unkeyedListEntries(UnkeyedListNode unkeyedListNode) {
        List<UnkeyedListEntryNode> unkeyedListEntries = Lists.newArrayList();

        for (UnkeyedListEntryNode unkeyedListEntryNode : unkeyedListNode.getValue()) {
            unkeyedListEntries.add(unkeyedListEntryNode);
        }

        return unkeyedListEntries;
    }

}
