/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;

@Beta
public final class OpaqueDataBuilder implements Builder<OpaqueData> {
    private OpaqueDataNode root;
    private boolean accurateLists = false;

    public OpaqueDataBuilder withAccurateLists(final boolean newAccurateLists) {
        this.accurateLists = newAccurateLists;
        return this;
    }

    public OpaqueDataBuilder withRoot(final OpaqueDataNode newRoot) {
        checkState(root == null, "Root node already set to %s", root);
        root = requireNonNull(newRoot);
        return this;
    }

    @Override
    public OpaqueData build() {
        checkState(root != null, "Root node not set");
        return new ImmutableOpaqueData(root, accurateLists);
    }
}
