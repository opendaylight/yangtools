/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration.Builder;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
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

    @Deprecated
    @Override
    public TipProducingDataTree create(final TreeType treeType) {
        return create(DataTreeConfiguration.getDefault(treeType));
    }

    @Deprecated
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

    @Override
    public TipProducingDataTree create(final DataTreeConfiguration treeConfig) {
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(createRoot(treeConfig.getRootPath()),
            Version.initial()), treeConfig, null);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final SchemaContext initialSchemaContext) {
        return create(treeConfig, initialSchemaContext, true);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final SchemaContext initialSchemaContext,
            final NormalizedNodeContainer<?, ?, ?> initialRoot) throws DataValidationFailedException {
        final DataTree ret = create(treeConfig, initialSchemaContext, false);

        final DataTreeModification mod = ret.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.EMPTY, initialRoot);
        mod.ready();

        ret.validate(mod);
        final DataTreeCandidate candidate = ret.prepare(mod);
        ret.commit(candidate);
        return ret;
    }

    private static DataTree create(final DataTreeConfiguration treeConfig, final SchemaContext initialSchemaContext,
            final boolean maskMandatory) {
        final DataSchemaNode rootSchemaNode = getRootSchemaNode(initialSchemaContext, treeConfig.getRootPath());
        final NormalizedNode<?, ?> rootDataNode = createRoot((DataNodeContainer)rootSchemaNode,
            treeConfig.getRootPath());
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(rootDataNode, Version.initial()), treeConfig,
            initialSchemaContext, rootSchemaNode, maskMandatory);
    }

    private static DataSchemaNode getRootSchemaNode(final SchemaContext schemaContext,
            final YangInstanceIdentifier rootPath) {
        final DataSchemaContextTree contextTree = DataSchemaContextTree.from(schemaContext);
        final DataSchemaContextNode<?> rootContextNode = contextTree.getChild(rootPath);
        Preconditions.checkArgument(rootContextNode != null, "Failed to find root %s in schema context", rootPath);

        final DataSchemaNode rootSchemaNode = rootContextNode.getDataSchemaNode();
        Preconditions.checkArgument(rootSchemaNode instanceof DataNodeContainer,
            "Root %s resolves to non-container type %s", rootPath, rootSchemaNode);
        return rootSchemaNode;
    }

    private static NormalizedNode<?, ?> createRoot(final DataNodeContainer schemaNode,
            final YangInstanceIdentifier path) {
        if (path.isEmpty()) {
            Preconditions.checkArgument(schemaNode instanceof ContainerSchemaNode,
                "Conceptual tree root has to be a container, not %s", schemaNode);
            return ROOT_CONTAINER;
        }

        final PathArgument arg = path.getLastPathArgument();
        if (schemaNode instanceof ContainerSchemaNode) {
            Preconditions.checkArgument(arg instanceof NodeIdentifier, "Mismatched container %s path %s", schemaNode,
                path);
            return ImmutableContainerNodeBuilder.create().withNodeIdentifier((NodeIdentifier) arg).build();
        } else if (schemaNode instanceof ListSchemaNode) {
            // This can either be a top-level list or its individual entry
            if (arg instanceof NodeIdentifierWithPredicates) {
                return ImmutableNodes.mapEntryBuilder().withNodeIdentifier((NodeIdentifierWithPredicates) arg).build();
            }
            Preconditions.checkArgument(arg instanceof NodeIdentifier, "Mismatched list %s path %s", schemaNode, path);
            return ImmutableNodes.mapNodeBuilder().withNodeIdentifier((NodeIdentifier) arg).build();
        } else {
            throw new IllegalArgumentException("Unsupported root schema " + schemaNode);
        }
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

    /**
     * Get an instance of this factory. This method cannot fail.
     *
     * @return Data tree factory instance.
     */
    public static InMemoryDataTreeFactory getInstance() {
        return INSTANCE;
    }
}
