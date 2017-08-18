/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * An implementation of apply operation which fails to do anything,
 * consistently. An instance of this class is used by the data tree
 * if it does not have a SchemaContext attached and hence cannot
 * perform anything meaningful.
 */
final class AlwaysFailOperation extends ModificationApplyOperation {
    static final ModificationApplyOperation INSTANCE = new AlwaysFailOperation();

    private AlwaysFailOperation() {

    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification,
            final Optional<TreeNode> storeMeta, final Version version) {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    void checkApplicable(final YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> storeMetadata, final Version version) {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    ChildTrackingPolicy getChildPolicy() {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        throw new IllegalStateException("Schema Context is not available.");
    }

    @Override
    void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        throw new IllegalStateException("Schema Context is not available.");
    }
}