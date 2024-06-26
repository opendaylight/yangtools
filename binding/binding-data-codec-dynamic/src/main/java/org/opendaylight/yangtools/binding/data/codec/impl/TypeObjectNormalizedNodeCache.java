/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingTypeObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A cache of NormalizedNodes corresponding to a particular TypeObject instantiation.
 */
final class TypeObjectNormalizedNodeCache<C extends CodecContext & BindingTypeObjectCodecTreeNode<TypeObject>>
        extends AbstractBindingNormalizedNodeCache<TypeObject, C> {
    TypeObjectNormalizedNodeCache(final C rootContext) {
        super(rootContext);
    }

    @Override
    public NormalizedNode load(final TypeObject key) {
        return rootContext().serialize(key);
    }
}
