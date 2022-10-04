/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;

public final class ImmutableAnyxmlNode
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, DOMSourceAnyxmlNode, DOMSource>
        implements DOMSourceAnyxmlNode {
    public ImmutableAnyxmlNode(final NodeIdentifier nodeIdentifier, final DOMSource value) {
        super(nodeIdentifier, value);
    }

    @Override
    protected Class<DOMSourceAnyxmlNode> implementedType() {
        return DOMSourceAnyxmlNode.class;
    }
}