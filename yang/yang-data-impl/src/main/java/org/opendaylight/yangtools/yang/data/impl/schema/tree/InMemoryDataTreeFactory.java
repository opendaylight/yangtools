/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A factory for creating in-memory data trees.
 */
public final class InMemoryDataTreeFactory implements DataTreeFactory {
    private static final InMemoryDataTreeFactory INSTANCE = new InMemoryDataTreeFactory();
    private final NormalizedNode<?, ?> root = ImmutableNodes.containerNode(SchemaContext.NAME);

    private InMemoryDataTreeFactory() {
        // Never instantiated externally
    }

    @Override
    public TipProducingDataTree create() {
        return create(TreeType.OPERATIONAL);
    }

    @Override
    public TipProducingDataTree create(TreeType treeType) {
        return new InMemoryDataTree(TreeNodeFactory.createTreeNode(root, Version.initial()), treeType, null);
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
