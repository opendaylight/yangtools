/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.MutableTreeNode;

final class UniqueChildMutableTreeNode extends ForwardingMutableTreeNode {
    private final Object vector;

    UniqueChildMutableTreeNode(final MutableTreeNode delegate, final Object vector) {
        super(delegate);
        this.vector = vector;
    }

    @Override
    public UniqueChildTreeNode seal() {
        // FIXME: extract new vector if needed
        return new UniqueChildTreeNode(sealDelegate(), vector);
    }
}
