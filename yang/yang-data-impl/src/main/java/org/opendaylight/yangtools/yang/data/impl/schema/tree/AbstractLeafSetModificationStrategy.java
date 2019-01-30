/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

abstract class AbstractLeafSetModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    AbstractLeafSetModificationStrategy(final LeafListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super((Class) LeafSetNode.class, treeConfig);
        entryStrategy = Optional.of(new LeafSetEntryModificationStrategy(schema));
    }

    @Override
    public final Optional<ModificationApplyOperation> getChild(final PathArgument identifier) {
        return identifier instanceof NodeWithValue ? entryStrategy : Optional.empty();
    }
}