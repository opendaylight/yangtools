/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@Beta
public final class OpaqueDataContainerBuilder extends AbstractOpaqueDataContainerBuilder<OpaqueDataContainer> {
    public OpaqueDataContainerBuilder() {

    }

    public OpaqueDataContainerBuilder(final int size) {
        super(size);
    }

    @Override
    public OpaqueDataContainerBuilder withChild(final OpaqueDataNode child) {
        super.withChild(child);
        return this;
    }

    @Override
    OpaqueDataContainer build(final NodeIdentifier identifier, final ImmutableList<@NonNull OpaqueDataNode> children) {
        return new ImmutableOpaqueDataContainer(identifier, children);
    }
}
