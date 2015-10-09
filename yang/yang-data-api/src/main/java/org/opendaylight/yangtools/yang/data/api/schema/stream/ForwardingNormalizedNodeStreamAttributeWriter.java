/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

abstract class ForwardingNormalizedNodeStreamAttributeWriter extends ForwardingNormalizedNodeStreamWriter implements NormalizedNodeStreamAttributeWriter {
    @Override
    protected abstract NormalizedNodeStreamAttributeWriter delegate();

    @Override
    public void leafNode(final NodeIdentifier name, final Object value, final Map<QName, String> attributes)
            throws IOException {
        delegate().leafNode(name, value, attributes);
    }

    @Override
    public void leafSetEntryNode(final Object value, final Map<QName, String> attributes) throws IOException {
        delegate().leafSetEntryNode(value, attributes);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint,
            final Map<QName, String> attributes) throws IOException {
        delegate().startContainerNode(name, childSizeHint, attributes);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint,
            final Map<QName, String> attributes) throws IOException {
        delegate().startUnkeyedListItem(name, childSizeHint, attributes);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint,
            final Map<QName, String> attributes) throws IOException {
        delegate().startMapEntryNode(identifier, childSizeHint, attributes);
    }
}
