/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.opaque;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@NonNullByDefault
final class ImmutableOpaqueData implements OpaqueData {
    private final OpaqueDataNode root;
    private final boolean accurateLists;

    ImmutableOpaqueData(final OpaqueDataNode root, final boolean accurateLists) {
        this.root = requireNonNull(root);
        this.accurateLists = accurateLists;
    }

    @Override
    public OpaqueDataNode getRoot() {
        return root;
    }

    @Override
    public boolean hasAccurateLists() {
        return accurateLists;
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OpaqueData)) {
            return false;
        }
        final OpaqueData other = (OpaqueData) obj;
        return accurateLists == other.hasAccurateLists() && root.equals(other.getRoot());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("root", root).add("accurateLists", accurateLists).toString();
    }
}
