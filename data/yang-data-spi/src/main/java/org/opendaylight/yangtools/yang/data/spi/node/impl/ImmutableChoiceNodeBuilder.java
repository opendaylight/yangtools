/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;

public final class ImmutableChoiceNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifier, ChoiceNode>
        implements ChoiceNode.Builder {
    public ImmutableChoiceNodeBuilder() {

    }

    public ImmutableChoiceNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    private ImmutableChoiceNodeBuilder(final ImmutableChoiceNode node) {
        super(node.name(), node.children);
    }

    public static ChoiceNode.@NonNull Builder create(final ChoiceNode node) {
        if (node instanceof ImmutableChoiceNode immutableNode) {
            return new ImmutableChoiceNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    @Override
    public ChoiceNode build() {
        return new ImmutableChoiceNode(getNodeIdentifier(), buildValue());
    }
}
