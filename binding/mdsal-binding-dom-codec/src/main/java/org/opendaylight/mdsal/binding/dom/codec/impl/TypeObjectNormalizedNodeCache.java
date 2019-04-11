/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

/**
 * A cache of NormalizedNodes corresponding to a particular TypeObject instantiation.
 */
final class TypeObjectNormalizedNodeCache
        extends AbstractBindingNormalizedNodeCache<TypeObject, ValueNodeCodecContext> {
    TypeObjectNormalizedNodeCache(final ValueNodeCodecContext rootContext) {
        super(rootContext);
    }

    @Override
    public NormalizedNode<?, ?> load(final TypeObject key) {
        return ImmutableNodes.leafNode(rootContext().getDomPathArgument(),
            rootContext().getValueCodec().serialize(key));
    }
}
