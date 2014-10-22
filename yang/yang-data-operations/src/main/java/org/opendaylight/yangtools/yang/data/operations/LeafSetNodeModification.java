/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class LeafSetNodeModification implements Modification<LeafListSchemaNode, LeafSetNode<?>> {

    @Override
    public Optional<LeafSetNode<?>> modify(LeafListSchemaNode schema, Optional<LeafSetNode<?>> actual,
            Optional<LeafSetNode<?>> modification, OperationStack operationStack) throws DataModificationException {

        List<LeafSetEntryNode<?>> resultNodes = Lists.newArrayList();
        if(actual.isPresent()) {
            Iterables.addAll(resultNodes, actual.get().getValue());
        }

        // TODO implement ordering for modification nodes

        for (LeafSetEntryNode<?> leafListModification : modification.get().getValue()) {
            operationStack.enteringNode(leafListModification);

            switch (operationStack.getCurrentOperation()) {
                case MERGE:
                case CREATE: {
                    DataModificationException.DataExistsException.check(schema.getQName(), actual, leafListModification);
                }
                case REPLACE: {
                    if (!contains(actual, leafListModification)) {
                        resultNodes.add(leafListModification);
                    }
                    break;
                }
                case DELETE: {
                    DataModificationException.DataMissingException.check(schema.getQName(), actual, leafListModification);
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
                default:
                    throw new UnsupportedOperationException(String.format("Unable to perform operation: %s on: %s, unknown", operationStack.getCurrentOperation(), schema));
            }

            operationStack.exitingNode(leafListModification);
        }
        return build(schema, resultNodes);
    }

    private Optional<LeafSetNode<?>> build(LeafListSchemaNode schemaNode, List<LeafSetEntryNode<?>> resultNodes) {
        if(resultNodes.isEmpty())
            return Optional.absent();

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> b = Builders.leafSetBuilder(schemaNode);
        for (LeafSetEntryNode<?> resultNode : resultNodes) {
            // FIXME: can we do something about this SuppressWarnings?
            @SuppressWarnings("unchecked")
            final LeafSetEntryNode<Object> child = (LeafSetEntryNode<Object>) resultNode;
            b.withChild(child);
        }

        return Optional.<LeafSetNode<?>>of(b.build());
    }

    private static boolean contains(Optional<LeafSetNode<?>> actual, LeafSetEntryNode<?> leafListModification) {
        boolean contains = actual.isPresent();
        contains &= actual.get().getChild(leafListModification.getIdentifier()).isPresent();

        return contains;
    }
}
