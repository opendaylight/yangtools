/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedAnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;

@SuppressWarnings("rawtypes")
@Beta
public class ImmutableNormalizedAnydataNodeBuilder
        extends AbstractImmutableAnydataNodeBuilder<NormalizedNodeContainer, NormalizedAnydataNode> {
    protected ImmutableNormalizedAnydataNodeBuilder() {

    }

    public static @NonNull NormalizedNodeBuilder<NodeIdentifier, NormalizedNodeContainer, NormalizedAnydataNode>
            create() {
        return new ImmutableNormalizedAnydataNodeBuilder();
    }

    @Override
    public NormalizedAnydataNode build() {
        return new ImmutableNormalizedAnydataNode(getNodeIdentifier(), getValue());
    }

    private static final class ImmutableNormalizedAnydataNode
            extends AbstractImmutableAnydataNode<NormalizedNodeContainer> implements NormalizedAnydataNode {
        ImmutableNormalizedAnydataNode(final NodeIdentifier nodeIdentifier, final NormalizedNodeContainer value) {
            super(nodeIdentifier, value);
        }
    }
}
