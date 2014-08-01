/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.util.List;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;

class CaseNodeCodecContext extends DataObjectCodecContext<ChoiceCaseNode> {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;

    CaseNodeCodecContext(final Class<?> cls, final ChoiceCaseNode nodeSchema,
            final CodecContextFactory runtimeContext) {
        super(cls, nodeSchema.getQName().getModule(), nodeSchema, runtimeContext);
        this.yangIdentifier = (new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName()));
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
    }

    @Override
    protected void addYangPathArgument(final PathArgument arg,
            final List<org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument> builder) {
        // NOOP
    }

}