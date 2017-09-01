/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnorderedMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    UnorderedMapModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(MapNode.class, treeConfig);
        entryStrategy = Optional.of(new ListEntryModificationStrategy(schema, treeConfig));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        // If the DataTree is rooted at a MapEntryNode the original value will be MapEntryNode
        // so make sure we can handle this aswell
        if (original instanceof MapNode) {
            return ImmutableMapNodeBuilder.create((MapNode) original);
        } else if (original instanceof MapEntryNode) {
            return ImmutableMapEntryNodeBuilder.create((MapEntryNode) original);
        }
        throw new IllegalArgumentException("MapModification strategy can only handle MapNode or MapEntryNode's, "
                + "offending node: " + original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        if (original instanceof MapNode) {
            return ImmutableMapNodeBuilder.create().withNodeIdentifier(((MapNode) original).getIdentifier()).build();
        } else if (original instanceof MapEntryNode) {
            return ImmutableMapEntryNodeBuilder.create().withNodeIdentifier(
                ((MapEntryNode) original).getIdentifier()).build();
        }
        throw new IllegalArgumentException("MapModification strategy can only handle MapNode or MapEntryNode's, "
                + "offending node: " + original);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            return entryStrategy;
        } else if (entryStrategy.isPresent()) {
            // In case we already are in a MapEntry node(for example DataTree rooted at MapEntry)
            // try to retrieve the child that the identifier should be pointing to from our entryStrategy
            // if we have one. If the entryStrategy cannot find this child we just return the absent
            // we get from it.
            return entryStrategy.get().getChild(identifier);
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "UnorderedMapModificationStrategy [entry=" + entryStrategy + "]";
    }
}
