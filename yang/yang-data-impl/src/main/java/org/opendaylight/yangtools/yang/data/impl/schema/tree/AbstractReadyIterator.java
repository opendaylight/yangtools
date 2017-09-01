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
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

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

    final AbstractReadyIterator process(final Version version) {
        // Walk all child nodes and remove any children which have not
        // been modified. If a child has children, we need to iterate
        // through it via re-entering this method on the child iterator.
        while (children.hasNext()) {
            final ModifiedNode child = children.next();
            final Optional<ModificationApplyOperation> childOperation = op.getChild(child.getIdentifier());
            Preconditions.checkState(childOperation.isPresent(), "Schema for child %s is not present.",
                    child.getIdentifier());
            final Collection<ModifiedNode> grandChildren = child.getChildren();
            final ModificationApplyOperation childOp = childOperation.get();

            if (grandChildren.isEmpty()) {
                // The child is empty, seal it
                child.seal(childOp, version);
                if (child.getOperation() == LogicalOperation.NONE) {
                    children.remove();
                }
            } else {
                return new NestedReadyIterator(this, child, grandChildren.iterator(), childOp);
            }
        }

        // We are done with this node, seal it.
        node.seal(op, version);

        // Remove from parent if we have one and this is a no-op
        if (node.getOperation() == LogicalOperation.NONE) {
            removeFromParent();
        }

        // Sub-iteration complete, return back to parent
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