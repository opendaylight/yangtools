/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.impl.SchemaAwareApplyOperation.TreeNodeSupport;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;

final class UniqueIndexTreeNodeSupport extends TreeNodeSupport {
    private final UniqueTreeNodeSupport<?> support;

    UniqueIndexTreeNodeSupport(final UniqueTreeNodeSupport<?> support) {
        this.support = requireNonNull(support);
    }

    @Override
    UniqueIndexTreeNode newTreeNode(final NormalizedNode newValue, final Version version) {
        return new UniqueIndexTreeNode(DEFAULT.newTreeNode(newValue, version));
    }

    @Override
    UniqueIndexMutableNode newMutableTreeNode(final NormalizedNode newValue, final Version version) {
        final UniqueIndexTreeNode prev = newTreeNode(newValue, version);
        return new UniqueIndexMutableNode(prev.delegate().mutable(), support, prev);
    }
}
