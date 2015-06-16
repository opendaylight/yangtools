/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Iterator;

abstract class AbstractReadyIterator {
    final Iterator<ModifiedNode> children;
    final ModifiedNode node;
    final ModificationApplyOperation op;

    private AbstractReadyIterator(final ModifiedNode node, final Iterator<ModifiedNode> children,
            final ModificationApplyOperation operation) {
        this.children = Preconditions.checkNotNull(children);
        this.node = Preconditions.checkNotNull(node);
        this.op = Preconditions.checkNotNull(operation);
    }

    static AbstractReadyIterator create(final ModifiedNode root, final ModificationApplyOperation operation) {
        return new RootReadyIterator(root, root.getChildren().iterator(), operation);
    }

    final AbstractReadyIterator process() {
        // Walk all child nodes and remove any children which have not
        // been modified. If a child
        while (children.hasNext()) {
            final ModifiedNode child = children.next();
            final Optional<ModificationApplyOperation> childOperation = op.getChild(child.getIdentifier());
            Preconditions.checkState(childOperation.isPresent(), "Schema for child %s is not present.",
                    child.getIdentifier());
            final Collection<ModifiedNode> grandChildren = child.getChildren();
            if (grandChildren.isEmpty()) {

                child.seal(childOperation.get());
                if (child.getOperation() == LogicalOperation.NONE) {
                    children.remove();
                }
            } else {
                return new NestedReadyIterator(this, child, grandChildren.iterator(), childOperation.get());
            }
        }

        node.seal(op);

        // Remove from parent if we have one and this is a no-op
        if (node.getOperation() == LogicalOperation.NONE) {
            removeFromParent();
        }
        return getParent();
    }

    abstract AbstractReadyIterator getParent();
    abstract void removeFromParent();

    private static final class NestedReadyIterator extends AbstractReadyIterator {
        private final AbstractReadyIterator parent;

        private NestedReadyIterator(final AbstractReadyIterator parent, final ModifiedNode node,
                final Iterator<ModifiedNode> children, final ModificationApplyOperation operation) {
            super(node, children, operation);
            this.parent = Preconditions.checkNotNull(parent);
        }

        @Override
        AbstractReadyIterator getParent() {
            return parent;
        }

        @Override
        void removeFromParent() {
            parent.children.remove();
        }
    }

    private static final class RootReadyIterator extends AbstractReadyIterator {
        private RootReadyIterator(final ModifiedNode node, final Iterator<ModifiedNode> children,
                final ModificationApplyOperation operation) {
            super(node, children, operation);
        }

        @Override
        AbstractReadyIterator getParent() {
            return null;
        }

        @Override
        void removeFromParent() {
            // No-op, since root node cannot be removed
        }
    }

}