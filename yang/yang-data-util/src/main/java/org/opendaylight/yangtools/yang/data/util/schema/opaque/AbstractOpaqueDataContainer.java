/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;

@NonNullByDefault
abstract class AbstractOpaqueDataContainer extends AbstractOpaqueDataNode {
    private final ImmutableList<OpaqueDataNode> children;

    AbstractOpaqueDataContainer(final OpaqueIdentifier identifier, final ImmutableList<OpaqueDataNode> children) {
        super(identifier);
        this.children = requireNonNull(children);
    }

    final ImmutableList<OpaqueDataNode> children() {
        return children;
    }

    @Override
    final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("children", children);
    }
}
