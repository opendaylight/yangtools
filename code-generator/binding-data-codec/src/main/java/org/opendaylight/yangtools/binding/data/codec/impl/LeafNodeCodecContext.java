/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class LeafNodeCodecContext extends NodeCodecContext {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<Object, Object> valueCodec;

    LeafNodeCodecContext(final DataSchemaNode node, final Codec<Object, Object> codec) {
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(node.getQName());
        this.valueCodec = codec;
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return (yangIdentifier);
    }

    protected Codec<Object, Object> getValueCodec() {
        return valueCodec;
    }

}