/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;

public final class ImmutableAnyxmlNodeBuilder
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode>
        implements AnyxmlNode.Builder<DOMSource, DOMSourceAnyxmlNode> {
    @Override
    public ImmutableAnyxmlNodeBuilder withValue(final DOMSource withValue) {
        super.withValue(withValue);
        return this;
    }

    @Override
    public DOMSourceAnyxmlNode build() {
        return new ImmutableDOMSourceAnyxmlNode(getNodeIdentifier(), getValue());
    }
}
