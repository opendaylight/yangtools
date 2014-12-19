/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

final class NotificationCodecContext extends DataObjectCodecContext<NotificationDefinition> {

    public NotificationCodecContext(final Class<?> key, final NotificationDefinition schema, final CodecContextFactory factory) {
        super(DataContainerCodecPrototype.from(key, schema, factory));
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> data) {
        Preconditions.checkState(data instanceof ContainerNode);
        return createBindingProxy((NormalizedNodeContainer<?, ?, ?>) data);
    }

}