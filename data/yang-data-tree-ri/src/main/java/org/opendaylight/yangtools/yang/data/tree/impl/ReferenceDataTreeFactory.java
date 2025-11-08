/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * A factory for creating in-memory data trees.
 */
@Component
@MetaInfServices
public sealed class ReferenceDataTreeFactory implements DataTreeFactory permits InMemoryDataTreeFactory {
    // FIXME: figure out how we can plug in custom factories
    private static final BuilderFactory BUILDERS = ImmutableNodes.builderFactory();
    // FIXME: YANGTOOLS-1074: we do not want this name
    private static final @NonNull ContainerNode ROOT_CONTAINER = BUILDERS.newContainerBuilder(0)
        .withNodeIdentifier(NodeIdentifier.create(SchemaContext.NAME))
        .build();

    /**
     * Default constructor.
     */
    @Activate
    public ReferenceDataTreeFactory() {
        // nothing else
    }

    @Override
    @Deprecated
    public final InMemoryDataTree create(final DataTreeConfiguration treeConfig) {
        return new InMemoryDataTree(TreeNode.of(createRoot(treeConfig.getRootPath()),
            Version.initial(treeConfig.isVersionInfoTrackingEnabled())), treeConfig, null);
    }

    @Override
    public final InMemoryDataTree create(final DataTreeConfiguration treeConfig,
            final EffectiveModelContext initialSchemaContext) {
        return createDataTree(treeConfig, initialSchemaContext, true);
    }

    @Override
    public final InMemoryDataTree create(final DataTreeConfiguration treeConfig,
            final EffectiveModelContext initialSchemaContext, final DistinctNodeContainer<?, ?> initialRoot)
                throws DataValidationFailedException {
        final var ret = createDataTree(treeConfig, initialSchemaContext, false);

        final var mod = ret.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(), initialRoot);
        mod.ready();

        ret.validate(mod);
        final var candidate = ret.prepare(mod);
        ret.commit(candidate);
        return ret;
    }

    private static @NonNull InMemoryDataTree createDataTree(final DataTreeConfiguration treeConfig,
            final EffectiveModelContext initialSchemaContext, final boolean maskMandatory) {
        final var rootPath = treeConfig.getRootPath();
        final var rootSchemaNode = getRootSchemaNode(initialSchemaContext, rootPath);
        final var rootDataNode = createRoot((DataNodeContainer) rootSchemaNode, rootPath);
        return new InMemoryDataTree(
            TreeNode.of(rootDataNode, Version.initial(treeConfig.isVersionInfoTrackingEnabled())), treeConfig,
            initialSchemaContext, rootSchemaNode, maskMandatory);
    }

    private static @NonNull NormalizedNode createRoot(final DataNodeContainer schemaNode,
            final YangInstanceIdentifier path) {
        final var arg = path.getLastPathArgument();
        if (arg == null) {
            if (schemaNode instanceof ContainerLike) {
                return ROOT_CONTAINER;
            }
            throw new IllegalArgumentException("Conceptual tree root has to be a container, not " + schemaNode);
        }

        return switch (schemaNode) {
            case ContainerSchemaNode containerSchema ->
                switch (arg) {
                    case NodeIdentifier nid -> BUILDERS.newContainerBuilder().withNodeIdentifier(nid).build();
                    default -> throw new IllegalArgumentException(
                        "Mismatched container " + schemaNode + " path " + path);
                };
            case ListSchemaNode listSchema ->
                switch (arg) {
                    // This can either be a top-level list or its individual entry
                    case NodeIdentifierWithPredicates nip ->
                        BUILDERS.newMapEntryBuilder().withNodeIdentifier(nip).build();
                    case NodeIdentifier nid -> {
                        final var builder = listSchema.isUserOrdered()
                            ? BUILDERS.newUserMapBuilder() : BUILDERS.newSystemMapBuilder();
                        yield builder.withNodeIdentifier(nid).build();
                    }
                    case NodeWithValue<?> var ->
                        throw new IllegalArgumentException("Mismatched list " + listSchema + " path " + path);
                };
            case null, default -> throw new IllegalArgumentException("Unsupported root schema " + schemaNode);
        };
    }

    private static @NonNull NormalizedNode createRoot(final YangInstanceIdentifier path) {
        return switch (path.getLastPathArgument()) {
            case null -> ROOT_CONTAINER;
            case NodeIdentifier nid -> BUILDERS.newContainerBuilder().withNodeIdentifier(nid).build();
            case NodeIdentifierWithPredicates nip -> BUILDERS.newMapEntryBuilder().withNodeIdentifier(nip).build();
            // FIXME: implement this leaf-lists
            case NodeWithValue<?> val -> throw new IllegalArgumentException("Unsupported root node " + val);
        };
    }

    private static DataSchemaNode getRootSchemaNode(final EffectiveModelContext schemaContext,
            final YangInstanceIdentifier rootPath) {
        final var contextTree = DataSchemaContextTree.from(schemaContext);
        final var rootContextNode = contextTree.childByPath(rootPath);
        if (rootContextNode == null) {
            throw new IllegalArgumentException("Failed to find root " + rootPath + " in schema context");
        }

        final var rootSchemaNode = rootContextNode.dataSchemaNode();
        if (rootSchemaNode instanceof DataNodeContainer) {
            return rootSchemaNode;
        }
        throw new IllegalArgumentException("Root " + rootPath + " resolves to non-container type " + rootSchemaNode);
    }
}
