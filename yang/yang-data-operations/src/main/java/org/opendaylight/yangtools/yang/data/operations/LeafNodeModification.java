/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

import com.google.common.base.Optional;

final class LeafNodeModification implements Modification<LeafSchemaNode, LeafNode<?>> {

    @Override
    public Optional<LeafNode<?>> modify(LeafSchemaNode schema, Optional<LeafNode<?>> actualNode,
                                    Optional<LeafNode<?>> modificationNode, OperationStack operationStack) throws DataModificationException {

        operationStack.enteringNode(modificationNode);

        Optional<LeafNode<?>> result;

        // Returns either actual node, modification node or empty in case of removal
        switch (operationStack.getCurrentOperation()) {
            case MERGE: {
                result = modificationNode.isPresent() ? modificationNode : actualNode;
                break;
            }
            case CREATE: {
                DataModificationException.DataExistsException.check(schema.getQName(), actualNode, null);
            }
            case REPLACE: {
                result = modificationNode;
                break;
            }
            case DELETE: {
                DataModificationException.DataMissingException.check(schema.getQName(), actualNode);
            }
            case REMOVE: {
                result = Optional.absent();
                break;
            }
            case NONE: {
                result = actualNode;
                break;
            }
            default:
                throw new UnsupportedOperationException(String.format("Unable to perform operation: %s on: %s, unknown", operationStack.getCurrentOperation(), schema));

        }

        operationStack.exitingNode(modificationNode);

        return result;
    }
}
