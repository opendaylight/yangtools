/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.AbstractNodeContainerModificationStrategy.Visible;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base strategy for applying changes to a ContainerNode, irrespective of its actual type.
 *
 * @param <T> Type of the container node
 */
sealed class DataNodeContainerModificationStrategy<T extends DataSchemaNode & DataNodeContainer> extends Visible<T>
        permits ContainerModificationStrategy, MapEntryModificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DataNodeContainerModificationStrategy.class);
    private static final VarHandle CHILDREN;

    static {
        try {
            CHILDREN = MethodHandles.lookup().findVarHandle(
                DataNodeContainerModificationStrategy.class, "children", ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull DataTreeConfiguration treeConfig;

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableMap<PathArgument, ModificationApplyOperation> children = ImmutableMap.of();

    DataNodeContainerModificationStrategy(final NormalizedNodeContainerSupport<?, ?> support, final T schema,
            final DataTreeConfiguration treeConfig) {
        super(support, treeConfig, schema);
        this.treeConfig = requireNonNull(treeConfig, "treeConfig");
    }

    @Override
    public final ModificationApplyOperation childByArg(final PathArgument arg) {
        final var local = (ImmutableMap<PathArgument, ModificationApplyOperation>) CHILDREN.getAcquire(this);
        final var existing = local.get(arg);
        if (existing != null) {
            return existing;
        }

        final var childOperation = resolveChild(arg);
        return childOperation != null ? appendChild(local, arg, childOperation) : null;
    }

    private ModificationApplyOperation resolveChild(final PathArgument identifier) {
        final var schema = getSchema();
        final var qname = identifier.getNodeType();
        final var child = schema.dataChildByName(qname);
        if (child == null) {
            LOG.trace("Child {} not present in container schema {} children {}", identifier, this,
                schema.getChildNodes());
            return null;
        }

        try {
            return SchemaAwareApplyOperation.from(child, treeConfig);
        } catch (ExcludedDataSchemaNodeException e) {
            LOG.trace("Failed to instantiate child {} in container schema {} children {}", identifier, this,
                schema.getChildNodes(), e);
            return null;
        }
    }

    private @Nullable ModificationApplyOperation appendChild(
            final ImmutableMap<PathArgument, ModificationApplyOperation> initial, final PathArgument identifier,
            final ModificationApplyOperation computed) {
        var previous = initial;
        while (true) {
            // Build up a new map based on observed snapshot and computed child
            final var updated = ImmutableMap
                .<PathArgument, ModificationApplyOperation>builderWithExpectedSize(previous.size() + 1)
                .putAll(previous)
                .put(identifier, computed)
                .build();

            // Attempt to install the updated map
            final var witness = (ImmutableMap<PathArgument, ModificationApplyOperation>)
                CHILDREN.compareAndExchangeRelease(this, previous, updated);
            if (witness == previous) {
                return computed;
            }

            // We have raced, acquire a new snapshot, recheck presence and retry if needed
            previous = witness;
            final var raced = previous.get(identifier);
            if (raced != null) {
                return raced;
            }
        }
    }
}
