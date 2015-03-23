/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Base strategy for applying changes to a ContainerNode, irrespective of its
 * actual type.
 *
 * @param <T> Type of the container node
 */
abstract class DataNodeContainerModificationStrategy<T extends DataNodeContainer> extends AbstractNodeContainerModificationStrategy {

    private final T schema;
    private final LoadingCache<PathArgument, ModificationApplyOperation> childCache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(new Function<PathArgument, ModificationApplyOperation>() {

                @Override
                public ModificationApplyOperation apply(final PathArgument identifier) {
                    if (identifier instanceof AugmentationIdentifier && schema instanceof AugmentationTarget) {
                        return SchemaAwareApplyOperation.from(schema, (AugmentationTarget) schema, (AugmentationIdentifier) identifier);
                    }

                    DataSchemaNode child = schema.getDataChildByName(identifier.getNodeType());
                    if (child == null) {
                        return null;
                    }
                    return SchemaAwareApplyOperation.from(child);
                }
            }));

    protected DataNodeContainerModificationStrategy(final T schema,
            final Class<? extends NormalizedNode<?, ?>> nodeClass) {
        super(nodeClass);
        this.schema = schema;
    }

    protected T getSchema() {
        return schema;
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument identifier) {
        try {
            return Optional.<ModificationApplyOperation> fromNullable(childCache.get(identifier));
        } catch (ExecutionException e) {
            return Optional.absent();
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected abstract DataContainerNodeBuilder createBuilder(NormalizedNode<?, ?> original);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + schema + "]";
    }
}