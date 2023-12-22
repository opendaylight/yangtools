/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Abstract base class for {@link UnkeyedListEntryNode} implementations.
 */
public abstract non-sealed class AbstractUnkeyedListEntryNode extends AbstractNormalizedNode<UnkeyedListEntryNode>
        implements UnkeyedListEntryNode {
    @Override
    protected final Class<UnkeyedListEntryNode> implementedType() {
        return contract();
    }
}
