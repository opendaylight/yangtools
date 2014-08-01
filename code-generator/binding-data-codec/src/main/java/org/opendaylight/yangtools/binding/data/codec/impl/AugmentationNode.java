/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;

public class AugmentationNode extends DataObjectCodecContext<AugmentationSchema> {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;

    public AugmentationNode(final Class<?> cls, final QNameModule namespace,
            final AugmentationIdentifier identifier, final AugmentationSchema nodeSchema,
            final CodecContextFactory loader) {
        super(cls, namespace, nodeSchema, loader);
        this.yangIdentifier = identifier;
    }

    @Override
    public YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
    }
}