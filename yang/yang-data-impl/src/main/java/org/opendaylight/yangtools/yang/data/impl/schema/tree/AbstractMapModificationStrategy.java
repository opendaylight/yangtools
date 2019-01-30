/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

// FIXME: createBuilder(), createEmptyValue() and getChild() here are hacks, originally introduced in
//        Change-Id: I9dc02a1917f38e8a0d62279843974b9869c48693. DataTreeRoot needs to be fixed up to properly
//        handle the lookup of through maps.
abstract class AbstractMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    final Optional<ModificationApplyOperation> entryStrategy;

    AbstractMapModificationStrategy(final Class<? extends MapNode> nodeClass, final ListSchemaNode schema,
            final DataTreeConfiguration treeConfig) {
        super(nodeClass, treeConfig);
        entryStrategy = Optional.of(ListEntryModificationStrategy.of(schema, treeConfig));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        return ImmutableMapEntryNodeBuilder.create(checkCast(original));
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        return ImmutableMapEntryNodeBuilder.create().withNodeIdentifier(checkCast(original).getIdentifier()).build();
    }

    @Override
    public final Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            return entryStrategy;
        }
        // In case we already are in a MapEntry node(for example DataTree rooted at MapEntry)
        // try to retrieve the child that the identifier should be pointing to from our entryStrategy
        // if we have one. If the entryStrategy cannot find this child we just return the absent
        // we get from it.
        return entryStrategy.get().getChild(identifier);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("entry", entryStrategy.get()).toString();
    }

    private static MapEntryNode checkCast(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof MapEntryNode,
            "MapModification strategy can only handle MapNode or MapEntryNodes, offending node: %s", original);
        return (MapEntryNode) original;
    }
}
