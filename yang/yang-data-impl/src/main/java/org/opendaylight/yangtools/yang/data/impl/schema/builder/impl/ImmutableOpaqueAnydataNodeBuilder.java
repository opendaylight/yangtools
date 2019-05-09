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
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;

@Beta
public class ImmutableOpaqueAnydataNodeBuilder extends AbstractImmutableAnydataNodeBuilder<OpaqueData> {
    protected ImmutableOpaqueAnydataNodeBuilder() {

    }

    public static @NonNull NormalizedNodeBuilder<NodeIdentifier, OpaqueData, AnydataNode<OpaqueData>> create() {
        return new ImmutableOpaqueAnydataNodeBuilder();
    }

    @Override
    public AnydataNode<OpaqueData> build() {
        return new ImmutableOpaqueAnydataNode<>(getNodeIdentifier(), getValue());
    }

    private static final class ImmutableOpaqueAnydataNode<T> extends AbstractImmutableAnydataNode<OpaqueData> {
        ImmutableOpaqueAnydataNode(final NodeIdentifier nodeIdentifier, final OpaqueData value) {
            super(nodeIdentifier, value);
        }

        @Override
        public Class<OpaqueData> getValueObjectModel() {
            return OpaqueData.class;
        }
    }
}
