/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating in-memory data trees.
 */
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class InMemoryDataTreeFactory implements DataTreeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDataTreeFactory.class);
    // FIXME: YANGTOOLS-1074: we do not want this name
    private static final @NonNull NormalizedNode<?, ?> ROOT_CONTAINER =
            ImmutableNodes.containerNode(SchemaContext.NAME);

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig) {
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(createRoot(treeConfig.getRootPath()),
            Version.initial()), treeConfig, null);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final EffectiveModelContext initialSchemaContext) {
        return createDataTree(treeConfig, initialSchemaContext, true);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final EffectiveModelContext initialSchemaContext,
            final NormalizedNodeContainer<?, ?, ?> initialRoot) throws DataValidationFailedException {
        final DataTree ret = createDataTree(treeConfig, initialSchemaContext, false);

        final DataTreeModification mod = ret.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.empty(), initialRoot);
        mod.ready();

        ret.validate(mod);
        final DataTreeCandidate candidate = ret.prepare(mod);
        ret.commit(candidate);
        return ret;
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("In-memory Data Tree activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("In-memory Data Tree deactivated");
    }

    private static @NonNull DataTree createDataTree(final DataTreeConfiguration treeConfig,
            final EffectiveModelContext initialSchemaContext, final boolean maskMandatory) {
        final DataSchemaNode rootSchemaNode = getRootSchemaNode(initialSchemaContext, treeConfig.getRootPath());
        final NormalizedNode<?, ?> rootDataNode = createRoot((DataNodeContainer)rootSchemaNode,
            treeConfig.getRootPath());
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(rootDataNode, Version.initial()), treeConfig,
            initialSchemaContext, rootSchemaNode, maskMandatory);
    }

    private static @NonNull NormalizedNode<?, ?> createRoot(final DataNodeContainer schemaNode,
            final YangInstanceIdentifier path) {
        if (path.isEmpty()) {
            checkArgument(schemaNode instanceof ContainerSchemaNode,
                "Conceptual tree root has to be a container, not %s", schemaNode);
            return ROOT_CONTAINER;
        }

        final PathArgument arg = path.getLastPathArgument();
        if (schemaNode instanceof ContainerSchemaNode) {
            checkArgument(arg instanceof NodeIdentifier, "Mismatched container %s path %s", schemaNode, path);
            return ImmutableContainerNodeBuilder.create().withNodeIdentifier((NodeIdentifier) arg).build();
        } else if (schemaNode instanceof ListSchemaNode) {
            // This can either be a top-level list or its individual entry
            if (arg instanceof NodeIdentifierWithPredicates) {
                return ImmutableNodes.mapEntryBuilder().withNodeIdentifier((NodeIdentifierWithPredicates) arg).build();
            }
            checkArgument(arg instanceof NodeIdentifier, "Mismatched list %s path %s", schemaNode, path);
            return ImmutableNodes.mapNodeBuilder().withNodeIdentifier((NodeIdentifier) arg).build();
        } else {
            throw new IllegalArgumentException("Unsupported root schema " + schemaNode);
        }
    }

    private static @NonNull NormalizedNode<?, ?> createRoot(final YangInstanceIdentifier path) {
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

    private static DataSchemaNode getRootSchemaNode(final EffectiveModelContext schemaContext,
            final YangInstanceIdentifier rootPath) {
        final DataSchemaContextTree contextTree = DataSchemaContextTree.from(schemaContext);
        final Optional<DataSchemaContextNode<?>> rootContextNode = contextTree.findChild(rootPath);
        checkArgument(rootContextNode.isPresent(), "Failed to find root %s in schema context", rootPath);

        final DataSchemaNode rootSchemaNode = rootContextNode.get().getDataSchemaNode();
        checkArgument(rootSchemaNode instanceof DataNodeContainer, "Root %s resolves to non-container type %s",
            rootPath, rootSchemaNode);
        return rootSchemaNode;
    }
}
