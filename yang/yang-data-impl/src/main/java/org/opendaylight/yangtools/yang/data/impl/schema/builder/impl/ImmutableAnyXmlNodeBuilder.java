/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedSimpleValueNode;

public class ImmutableAnyXmlNodeBuilder
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, DOMSource, AnyXmlNode> {

    public static @NonNull NormalizedNodeBuilder<NodeIdentifier, DOMSource, AnyXmlNode> create() {
        return new ImmutableAnyXmlNodeBuilder();
    }

    @Override
    public AnyXmlNode build() {
        return new ImmutableXmlNode(getNodeIdentifier(), getValue());
    }

    private static final class ImmutableXmlNode
            extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, DOMSource> implements AnyXmlNode {

        ImmutableXmlNode(final NodeIdentifier nodeIdentifier, final DOMSource value) {
            super(nodeIdentifier, value);
        }
    }
}
