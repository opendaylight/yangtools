/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

class ContainerNodeCodecContext extends DataObjectCodecContext<ContainerSchemaNode> {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;

    protected ContainerNodeCodecContext(final Class<?> cls, final ContainerSchemaNode nodeSchema,
            final CodecContextFactory loader) {
        super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
        this.yangIdentifier = (new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName()));
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
    }

}