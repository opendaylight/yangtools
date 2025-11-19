/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.ForeignDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData.Builder;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

/**
 * A {@link NormalizedYangData.Builder} producing immutable on-heap {@link NormalizedYangData} instances.
 */
final class ImmutableYangDataBuilder implements NormalizedYangData.Builder {
    private final @NonNull YangDataName name;

    private DataContainerChild child;

    ImmutableYangDataBuilder(final YangDataName name) {
        this.name = requireNonNull(name);
    }

    @Override
    public Builder setContainerDataNode(final ChoiceNode containerDataNode) {
        return setChild(containerDataNode);
    }

    @Override
    public Builder setContainerDataNode(final ContainerNode containerDataNode) {
        return setChild(containerDataNode);
    }

    @Override
    public Builder setContainerDataNode(final ForeignDataNode<?> containerDataNode) {
        return setChild(containerDataNode);
    }

    @Override
    public @NonNull Builder setContainerDataNode(final MapNode containerDataNode) {
        return setChild(containerDataNode);
    }

    @Override
    public Builder setContainerDataNode(final UnkeyedListNode containerDataNode) {
        return setChild(containerDataNode);
    }

    @Override
    public NormalizedYangData build() {
        if (child == null) {
            throw new IllegalStateException("Missing containerDataNode");
        }
        return new ImmutableYangData(name, child);
    }

    @NonNullByDefault
    private Builder setChild(final DataContainerChild child) {
        this.child = requireNonNull(child);
        return this;
    }
}
