/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ContainerModificationStrategy extends AbstractDataNodeContainerModificationStrategy<ContainerSchemaNode> {
    private final Collection<YangInstanceIdentifier> mandatoryNodes;

    ContainerModificationStrategy(final ContainerSchemaNode schemaNode, final TreeType treeType) {
        super(schemaNode, ContainerNode.class, treeType);

        final Builder<YangInstanceIdentifier> builder = ImmutableList.builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.EMPTY, schemaNode);
        mandatoryNodes = builder.build();
    }

    private static void findMandatoryNodes(final Builder<YangInstanceIdentifier> builder,
            final YangInstanceIdentifier id, final DataNodeContainer node) {
        for (DataSchemaNode child : node.getChildNodes()) {
            if (child instanceof ContainerSchemaNode) {
                final ContainerSchemaNode container = (ContainerSchemaNode) child;
                if (!container.isPresenceContainer()) {
                    findMandatoryNodes(builder, id.node(child.getQName()), container);
                }
            } else {
                final ConstraintDefinition constraints = child.getConstraints();
                final Integer minElements = constraints.getMinElements();
                if (constraints.isMandatory() || (minElements != null && minElements > 0)) {
                    builder.add(id.node(child.getQName()).toOptimized());
                }
            }
        }
    }

    private final void checkMandatoryNodes(final TreeNode tree) {
        final NormalizedNode<?, ?> data = tree.getData();
        for (YangInstanceIdentifier id : mandatoryNodes) {
            final Optional<NormalizedNode<?, ?>> descandant = NormalizedNodes.findNode(data, id);
            Preconditions.checkArgument(descandant.isPresent(), "Node %s is missing mandatory descendant %s",
                    tree.getIdentifier(), id);
        }
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyMerge(modification, currentMeta, version);
        checkMandatoryNodes(ret);
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        final TreeNode ret = super.applyWrite(modification, currentMeta, version);
        checkMandatoryNodes(ret);
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyTouch(modification, currentMeta, version);
        checkMandatoryNodes(ret);
        return ret;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ContainerNode);
        return ImmutableContainerNodeBuilder.create((ContainerNode) original);
    }
}
