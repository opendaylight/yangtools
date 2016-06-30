/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeIndex;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class UniqueConstraintEnforcer implements Immutable {
    protected abstract Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> createIndexesFromData(
            final NormalizedNode<?, ?> newValue);

    protected abstract Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> updateIndexes(
            Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes, NormalizedNode<?, ?> data);

    protected abstract Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> removeFromIndexes(
            Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes, PathArgument id);

    static UniqueConstraintEnforcer forList(final ListSchemaNode schema, final DataTreeConfiguration config) {
        switch (config.getTreeType()) {
        case CONFIGURATION:
            return !config.isUniqueIndexEnabled() || schema.getUniqueConstraints().isEmpty() ?
                    NoOpUniqueConstraintEnforcer.INSTANCE :
                        new StrictUniqueConstraintEnforcer(schema.getUniqueConstraints());
        case OPERATIONAL:
            return NoOpUniqueConstraintEnforcer.INSTANCE;
        default:
            throw new UnsupportedOperationException(String.format("Not supported tree type %s", config.getTreeType()));
        }
    }

    abstract protected List<Set<YangInstanceIdentifier>> getUniqueConstraintsLeafIds();
}
