/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

public class ImmutableAnyXmlNodeBuilder extends AbstractImmutableNormalizedNodeBuilder<YangInstanceIdentifier.NodeIdentifier, Node<?>, AnyXmlNode> {

    public static NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, Node<?>, AnyXmlNode> create() {
        return new ImmutableAnyXmlNodeBuilder();
    }

    @Override
    public AnyXmlNode build() {
        return new ImmutableXmlNode(getNodeIdentifier(), getValue(), getAttributes());
    }

    private static final class ImmutableXmlNode extends AbstractImmutableNormalizedValueAttrNode<YangInstanceIdentifier.NodeIdentifier, Node<?>> implements AnyXmlNode {

        ImmutableXmlNode(final YangInstanceIdentifier.NodeIdentifier nodeIdentifier, final Node<?> value, final Map<QName, String> attributes) {
            super(nodeIdentifier, value, attributes);
        }
    }
}
