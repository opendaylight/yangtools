/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks netconf operations on nested nodes.
 */
final class OperationStack {

    private static final Logger LOG = LoggerFactory.getLogger(OperationStack.class);
    private static final QName OPERATION_NAME = new QName(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), "operation");

    private final Deque<ModifyAction> operations = new LinkedList<>();

    public OperationStack(ModifyAction operation) {
        operations.add(operation);
    }

    public void enteringNode(Optional<? extends NormalizedNode<?, ?>> modificationNode) {
        if (!modificationNode.isPresent()) {
            return;
        }

        NormalizedNode<?, ?> modification = modificationNode.get();

        enteringNode(modification);
    }

    public void enteringNode(NormalizedNode<?, ?> modificationNode) {
        ModifyAction operation = getOperation(modificationNode);
        if (operation == null) {
            return;
        }

        addOperation(operation);
    }

    private ModifyAction getOperation(NormalizedNode<?, ?> modificationNode) {
        if (!(modificationNode instanceof AttributesContainer)) {
            return null;
        }

        String operationString = ((AttributesContainer) modificationNode).getAttributes().get(OPERATION_NAME);

        return operationString == null ? null : ModifyAction.fromXmlValue(operationString);
    }

    private void addOperation(ModifyAction operation) {
        // Add check for permitted operation
        operations.add(operation);
        LOG.trace("Operation added {}, {}", operation, this);
    }

    public ModifyAction getCurrentOperation() {
        return operations.getLast();
    }

    public void exitingNode(Optional<? extends NormalizedNode<?, ?>> modificationNode) {
        if (!modificationNode.isPresent()) {
            return;
        }

        NormalizedNode<?, ?> modification = modificationNode.get();

        exitingNode(modification);
    }

    public void exitingNode(NormalizedNode<?, ?> modification) {
        ModifyAction operation = getOperation(modification);
        if (operation == null) {
            return;
        }

        Preconditions.checkState(operations.size() > 1);
        Preconditions.checkState(operations.peekLast().equals(operation), "Operations mismatch %s, %s",
                operations.peekLast(), operation);

        ModifyAction removed = operations.removeLast();
        LOG.trace("Operation removed {}, {}", removed, this);
    }

    @Override
	public String toString() {
        return operations.toString();
    }

}
