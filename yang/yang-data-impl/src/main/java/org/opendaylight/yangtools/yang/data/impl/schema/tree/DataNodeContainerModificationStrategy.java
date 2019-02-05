/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base strategy for applying changes to a ContainerNode, irrespective of its
 * actual type.
 *
 * @param <T> Type of the container node
 */
class DataNodeContainerModificationStrategy<T extends DataNodeContainer>
        extends AbstractNodeContainerModificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DataNodeContainerModificationStrategy.class);

    private final DataTreeConfiguration treeConfig;
    private final T schema;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DataNodeContainerModificationStrategy, ImmutableMap> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DataNodeContainerModificationStrategy.class, ImmutableMap.class,
                "children");
    private volatile ImmutableMap<PathArgument, ModificationApplyOperation> children = ImmutableMap.of();

    DataNodeContainerModificationStrategy(final NormalizedNodeContainerSupport<?, ?> support, final T schema,
            final DataTreeConfiguration treeConfig) {
        super(support, treeConfig);
        this.schema = requireNonNull(schema, "schema");
        this.treeConfig = requireNonNull(treeConfig, "treeConfig");
    }

    @Override
    public final Optional<ModificationApplyOperation> getChild(final PathArgument identifier) {
        final ImmutableMap<PathArgument, ModificationApplyOperation> local = children;
        final ModificationApplyOperation existing = local.get(identifier);
        if (existing != null) {
            return Optional.of(existing);
        }

        if (identifier instanceof AugmentationIdentifier && schema instanceof AugmentationTarget) {
            return appendChild(local, identifier, SchemaAwareApplyOperation.from(schema, (AugmentationTarget) schema,
                (AugmentationIdentifier) identifier, treeConfig));
        }

        final QName qname = identifier.getNodeType();
        final Optional<DataSchemaNode> child = schema.findDataChildByName(qname);
        if (!child.isPresent()) {
            LOG.trace("Child {} not present in container schema {} children {}", identifier, this,
                schema.getChildNodes());
            return Optional.empty();
        }

        return appendChild(local, identifier, SchemaAwareApplyOperation.from(child.get(), treeConfig));
    }

    private Optional<ModificationApplyOperation> appendChild(
            final ImmutableMap<PathArgument, ModificationApplyOperation> initial, final PathArgument identifier,
            final ModificationApplyOperation computed) {

        ImmutableMap<PathArgument, ModificationApplyOperation> previous = initial;
        while (true) {
            // Build up a new map based on observed snapshot and computed child
            final Builder<PathArgument, ModificationApplyOperation> builder = ImmutableMap.builderWithExpectedSize(
                previous.size() + 1);
            builder.putAll(previous);
            builder.put(identifier, computed);
            final ImmutableMap<PathArgument, ModificationApplyOperation> updated = builder.build();

            // Attempt to install the updated map
            if (UPDATER.compareAndSet(this, previous, updated)) {
                return Optional.of(computed);
            }

            // We have raced, acquire a new snapshot, recheck presence and retry if needed
            previous = children;
            final ModificationApplyOperation raced = previous.get(identifier);
            if (raced != null) {
                return Optional.of(raced);
            }
        }
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("schema", schema);
    }
}
