/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

public abstract class AbstractImmutableDataContainerAttrNode<K extends InstanceIdentifier.PathArgument>
        extends AbstractImmutableDataContainerNode<K>
    implements AttributesContainer {

    private final Map<QName, String> attributes;

    public AbstractImmutableDataContainerAttrNode(
            Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children,
            K nodeIdentifier, Map<QName, String> attributes) {
        super(children, nodeIdentifier);
        this.attributes = attributes;
    }

    @Override
    public Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public Object getAttributeValue(QName value) {
        return attributes.get(value);
    }
}
