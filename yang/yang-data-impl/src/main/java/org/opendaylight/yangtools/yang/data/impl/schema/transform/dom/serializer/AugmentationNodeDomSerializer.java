/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.AugmentationNodeBaseSerializer;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.w3c.dom.Element;

@Deprecated
final class AugmentationNodeDomSerializer extends
        AugmentationNodeBaseSerializer<Element> {

    private final NodeSerializerDispatcher<Element> dispatcher;

    AugmentationNodeDomSerializer(final NodeSerializerDispatcher<Element> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    @Override
    protected NodeSerializerDispatcher<Element> getNodeDispatcher() {
        return dispatcher;
    }
}
