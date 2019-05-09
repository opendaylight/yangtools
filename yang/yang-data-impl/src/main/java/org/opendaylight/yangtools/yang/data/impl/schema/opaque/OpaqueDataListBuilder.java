/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.opaque;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataList;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@Beta
public final class OpaqueDataListBuilder extends AbstractOpaqueDataContainerBuilder<OpaqueDataList> {
    public OpaqueDataListBuilder() {

    }

    public OpaqueDataListBuilder(final int size) {
        super(size);
    }

    @Override
    public OpaqueDataListBuilder withChild(final OpaqueDataNode child) {
        super.withChild(child);
        return this;
    }

    @Override
    OpaqueDataList build(final NodeIdentifier identifier, final ImmutableList<OpaqueDataNode> children) {
        for (OpaqueDataNode child : children) {
            checkState(identifier.equals(child.getIdentifier()), "Child %s does not match list identifier %s", child,
                identifier);
        }
        return new ImmutableOpaqueDataList(identifier, children);
    }
}
