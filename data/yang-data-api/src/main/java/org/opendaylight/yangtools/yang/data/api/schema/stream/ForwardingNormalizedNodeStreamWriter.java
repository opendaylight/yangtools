/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.collect.ForwardingObject;
import java.io.IOException;
import java.util.Collection;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

public abstract class ForwardingNormalizedNodeStreamWriter extends ForwardingObject
        implements NormalizedNodeStreamWriter {
    @Override
    protected abstract NormalizedNodeStreamWriter delegate();

    @Override
    public <T extends Extension> @Nullable T extension(final Class<T> type) {
        return delegate().extension(type);
    }

    @Override
    public Collection<? extends Extension> supportedExtensions() {
        return delegate().supportedExtensions();
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        delegate().startLeafNode(name);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startLeafSet(name, childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startOrderedLeafSet(name, childSizeHint);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        delegate().startLeafSetEntryNode(name);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startContainerNode(name, childSizeHint);
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startUnkeyedList(name, childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startUnkeyedListItem(name, childSizeHint);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startMapNode(name, childSizeHint);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        delegate().startMapEntryNode(identifier, childSizeHint);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startOrderedMapNode(name, childSizeHint);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        delegate().startChoiceNode(name, childSizeHint);
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        return delegate().startAnyxmlNode(name, objectModel);
    }

    @Override
    public boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        return delegate().startAnydataNode(name, objectModel);
    }

    @Override
    public void endNode() throws IOException {
        delegate().endNode();
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        delegate().scalarValue(value);
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        delegate().domSourceValue(value);
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }
}
