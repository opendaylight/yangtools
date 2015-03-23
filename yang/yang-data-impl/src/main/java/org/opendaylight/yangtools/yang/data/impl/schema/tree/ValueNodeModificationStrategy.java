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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class ValueNodeModificationStrategy<T extends DataSchemaNode> extends SchemaAwareApplyOperation {
    private final Class<? extends NormalizedNode<?, ?>> nodeClass;
    private final T schema;

    protected ValueNodeModificationStrategy(final T schema, final Class<? extends NormalizedNode<?, ?>> nodeClass) {
        this.schema = schema;
        this.nodeClass = nodeClass;
    }

    @Override
    protected void verifyWrittenStructure(final NormalizedNode<?, ?> writtenValue) {
        checkArgument(nodeClass.isInstance(writtenValue), "Node should must be of type %s", nodeClass);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        throw new UnsupportedOperationException("Node " + schema.getPath()
                + "is leaf type node. Child nodes not allowed");
    }

    @Override
    protected TreeNode applySubtreeChange(final ModifiedNode modification,
            final TreeNode currentMeta, final Version version) {
        throw new UnsupportedOperationException("Node " + schema.getPath()
                + "is leaf type node. Subtree change is not allowed.");
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
            final Version version) {
        // Just overwrite whatever was there
        modification.resolveModificationType(ModificationType.WRITE);
        return applyWrite(modification, null, version);
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification,
            final Optional<TreeNode> currentMeta, final Version version) {
        return TreeNodeFactory.createTreeNodeRecursively(modification.getWrittenValue(), version);
    }

    @Override
    protected void checkSubtreeModificationApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws IncorrectDataStructureException {
        throw new IncorrectDataStructureException(path, "Subtree modification is not allowed.");
    }
}