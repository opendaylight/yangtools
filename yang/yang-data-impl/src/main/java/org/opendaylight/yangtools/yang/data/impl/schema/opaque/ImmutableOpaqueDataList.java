/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.opaque;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataList;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@NonNullByDefault
final class ImmutableOpaqueDataList extends AbstractOpaqueDataContainer implements OpaqueDataList {
    ImmutableOpaqueDataList(final NodeIdentifier identifier, final ImmutableList<OpaqueDataNode> children) {
        super(identifier, children);
    }

    @Override
    public ImmutableList<OpaqueDataNode> getChildren() {
        return children();
    }

    @Override
    public int hashCode() {
        return 31 * getIdentifier().hashCode() + children().hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpaqueDataContainer)) {
            return false;
        }
        final OpaqueDataList other = (OpaqueDataList) obj;
        return getIdentifier().equals(other.getIdentifier()) && children().equals(other.getChildren());
    }
}
