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
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.ri.node.impl.ImmutableXmlNode;

public class ImmutableAnyXmlNodeBuilder
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> {

    public static @NonNull NormalizedNodeBuilder<NodeIdentifier, DOMSource, DOMSourceAnyxmlNode> create() {
        return new ImmutableAnyXmlNodeBuilder();
    }

    @Override
    public ImmutableAnyXmlNodeBuilder withValue(final DOMSource withValue) {
        super.withValue(withValue);
        return this;
    }

    @Override
    public DOMSourceAnyxmlNode build() {
        return new ImmutableXmlNode(getNodeIdentifier(), getValue());
    }
}
