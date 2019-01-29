/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;

// FIXME: YANGTOOLS-941: this is a transitional class until we can push support to superclass, which requires
//                       figuring out the LeafSet type safety story :(
abstract class AbstractNodeContainerSupportModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final NormalizedNodeContainerSupport<?, ?> support;

    AbstractNodeContainerSupportModificationStrategy(final Class<? extends NormalizedNode<?, ?>> nodeClass,
            final NormalizedNodeContainerSupport<?, ?> support, final DataTreeConfiguration treeConfig) {
        super(nodeClass, treeConfig);
        this.support = requireNonNull(support);
    }

    @Override
    protected final NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        return support.createBuilder(original);
    }

    @Override
    protected final NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        return support.createEmptyValue(original);
    }
}
