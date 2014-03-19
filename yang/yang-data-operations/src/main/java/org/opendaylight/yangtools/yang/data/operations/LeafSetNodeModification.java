/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class LeafSetNodeModification implements Modification<LeafListSchemaNode, LeafSetNode<?>> {

    @Override
    public Optional<LeafSetNode<?>> modify(LeafListSchemaNode schemaNode, Optional<LeafSetNode<?>> actual,
            Optional<LeafSetNode<?>> modification, OperationStack operations) throws DataModificationException {

        Preconditions.checkArgument(actual.isPresent() || modification.isPresent(),
                "Either actual or modification node has to be present for: %s", schemaNode);

        // Merge or None operation on parent, leaving actual if modification not present
        if(modification.isPresent() == false)
            return actual;

        List<LeafSetEntryNode<?>> resultNodes = Lists.newArrayList();
        if(actual.isPresent()) {
            Iterables.addAll(resultNodes, actual.get().getValue());
        }

        // TODO implement ordering for modification nodes

        for (LeafSetEntryNode<?> leafListModification : modification.get().getValue()) {
            operations.enteringNode(leafListModification);

            switch (operations.getCurrentOperation()) {
                case MERGE:
                case CREATE: {
                    DataModificationException.DataExistsException.check(schemaNode.getQName(), actual, leafListModification);
                }
                case REPLACE: {
                    if (contains(actual, leafListModification) == false) {
                        resultNodes.add(leafListModification);
                    }
                    break;
                }
                case DELETE: {
                    DataModificationException.DataMissingException.check(schemaNode.getQName(), actual, leafListModification);
                }
                case REMOVE: {
                    if (resultNodes.contains(leafListModification)) {
                        resultNodes.remove(leafListModification);
                    }
                    break;
                }
                case NONE: {
                    break;
                }
            }

            operations.exitingNode(leafListModification);
        }
        return build(schemaNode, resultNodes);
    }

    private Optional<LeafSetNode<?>> build(LeafListSchemaNode schemaNode, List<LeafSetEntryNode<?>> resultNodes) {
        if(resultNodes.isEmpty())
            return Optional.absent();

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> b = Builders.leafSetBuilder(schemaNode);
        for (LeafSetEntryNode<?> resultNode : resultNodes) {
            // TODO fix generic warning
            b.withChild((LeafSetEntryNode<Object>) resultNode);
        }

        return Optional.<LeafSetNode<?>>of(b.build());
    }

    private static boolean contains(Optional<LeafSetNode<?>> actual, LeafSetEntryNode<?> leafListModification) {
        boolean contains = actual.isPresent();
        contains &= actual.get().getChild(leafListModification.getIdentifier()).isPresent();

        return contains;
    }
}
