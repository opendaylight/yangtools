/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import java.net.URI;
import java.util.Deque;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Tracks netconf operations on nested nodes.
 */
public interface OperationStack {

    void enteringNode(Optional<? extends NormalizedNode<?, ?>> modificationNode);

    void enteringNode(NormalizedNode<?, ?> modificationNode);

    ModifyAction getCurrentOperation();

    void exitingNode(Optional<? extends NormalizedNode<?, ?>> modificationNode);

    void exitingNode(NormalizedNode<?, ?> modificationNode);

    class OperationStackImpl implements OperationStack {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OperationStackImpl.class);

        final Deque<ModifyAction> operations = Lists.newLinkedList();
        private static final QName OPERATION_NAME;

        static {
            OPERATION_NAME = new QName(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), "operation");
        }

        public OperationStackImpl(ModifyAction operation) {
            operations.add(operation);
        }

        @Override
        public void enteringNode(Optional<? extends NormalizedNode<?, ?>> modificationNode) {
            if(modificationNode.isPresent() == false) {
                return;
            }

            NormalizedNode<?, ?> modification = modificationNode.get();

            enteringNode(modification);
        }

        @Override
        public void enteringNode(NormalizedNode<?, ?> modificationNode) {
            ModifyAction operation = getOperation(modificationNode);
            if (operation == null) {
                return;
            }

            addOperation(operation);
        }

        private ModifyAction getOperation(NormalizedNode<?, ?> modificationNode) {
            Preconditions.checkArgument(modificationNode instanceof AttributesContainer, "Trying to retrieve attributes from: %s, not: %s", modificationNode, AttributesContainer.class);

            String operationString = ((AttributesContainer) modificationNode).getAttributes().get(OPERATION_NAME);

            return operationString == null ? null : ModifyAction.fromXmlValue(operationString);
        }

        private void addOperation(ModifyAction operation) {
            // Add check for permitted operation
            operations.add(operation);
            logger.trace("Operation added {}, {}", operation, this);
        }

        @Override
        public ModifyAction getCurrentOperation() {
            return operations.getLast();
        }

        @Override
        public void exitingNode(Optional<? extends NormalizedNode<?, ?>> modificationNode) {
            if(modificationNode.isPresent() == false) {
                return;
            }

            NormalizedNode<?, ?> modification = modificationNode.get();

            exitingNode(modification);
        }

        @Override
        public void exitingNode(NormalizedNode<?, ?> modification) {
            ModifyAction operation = getOperation(modification);
            if (operation == null) {
                return;
            }

            Preconditions.checkState(operations.size() > 1);
            Preconditions.checkState(operations.peekLast().equals(operation), "Operations mismatch %s, %s",
                    operations.peekLast(), operation);

            ModifyAction removed = operations.removeLast();
            logger.trace("Operation removed {}, {}", removed, this);
        }

        @Override
        public String toString() {
            return operations.toString();
        }

    }

}
