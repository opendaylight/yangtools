/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration.Builder;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A factory for creating in-memory data trees.
 */
public final class InMemoryDataTreeFactory implements DataTreeFactory {
    private static final InMemoryDataTreeFactory INSTANCE = new InMemoryDataTreeFactory();
    private static final NormalizedNode<?, ?> ROOT_CONTAINER = ImmutableNodes.containerNode(SchemaContext.NAME);

    private InMemoryDataTreeFactory() {
        // Never instantiated externally
    }

    @Override
    public TipProducingDataTree create(final TreeType treeType) {
        return create(DataTreeConfiguration.getDefault(treeType));
    }

    @Override
    public TipProducingDataTree create(final DataTreeConfiguration treeConfig) {
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(createRoot(treeConfig.getRootPath()),
            Version.initial()), treeConfig, null);
    }

    private static NormalizedNode<?, ?> createRoot(final YangInstanceIdentifier path) {
        if (path.isEmpty()) {
            return ROOT_CONTAINER;
        }

        final PathArgument arg = path.getLastPathArgument();
        if (arg instanceof NodeIdentifier) {
            return ImmutableContainerNodeBuilder.create().withNodeIdentifier((NodeIdentifier) arg).build();
        }
        if (arg instanceof NodeIdentifierWithPredicates) {
            return ImmutableNodes.mapEntryBuilder().withNodeIdentifier((NodeIdentifierWithPredicates) arg).build();
        }

        // FIXME: implement augmentations and leaf-lists
        throw new IllegalArgumentException("Unsupported root node " + arg);
    }

    @Override
    public TipProducingDataTree create(final TreeType treeType, final YangInstanceIdentifier rootPath) {
        if (rootPath.isEmpty()) {
            return create(treeType);
        }

        final DataTreeConfiguration defConfig = DataTreeConfiguration.getDefault(treeType);
        final DataTreeConfiguration config;
        if (!rootPath.isEmpty()) {
            config = new Builder(treeType).setMandatoryNodesValidation(defConfig.isMandatoryNodesValidationEnabled())
                    .setRootPath(rootPath).setUniqueIndexes(defConfig.isUniqueIndexEnabled()).build();
        } else {
            config = defConfig;
        }

        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(createRoot(rootPath), Version.initial()), config,
            null);
    }

    /**
     * Get an instance of this factory. This method cannot fail.
     *
     * @return Data tree factory instance.
     */
    public static InMemoryDataTreeFactory getInstance() {
        return INSTANCE;
    }
}
