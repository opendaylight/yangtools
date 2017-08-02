/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.io.IOException;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

//FIXME: Consider moving this to yang.binding.util.* in Be
abstract class ForwardingBindingStreamEventWriter implements BindingStreamEventWriter {

    protected abstract BindingStreamEventWriter delegate();

    @Override
    public void leafNode(final String localName, final Object value) throws IOException {
        delegate().leafNode(localName, value);
    }

    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException {
        delegate().startLeafSet(localName, childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final String localName, final int childSizeHint) throws IOException {
        delegate().startOrderedLeafSet(localName, childSizeHint);
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IOException {
        delegate().leafSetEntryNode(value);
    }

    @Override
    public void startContainerNode(final Class<? extends DataObject> container, final int childSizeHint)
            throws IOException {
        delegate().startContainerNode(container, childSizeHint);
    }

    @Override
    public void startUnkeyedList(final Class<? extends DataObject> localName, final int childSizeHint)
            throws IOException {
        delegate().startUnkeyedList(localName, childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IOException {
        delegate().startUnkeyedListItem(childSizeHint);
    }

    @Override
    public <T extends DataObject & Identifiable<?>> void startMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException {
        delegate().startMapNode(mapEntryType, childSizeHint);
    }

    @Override
    public <T extends DataObject & Identifiable<?>> void startOrderedMapNode(final Class<T> mapEntryType,
            final int childSizeHint) throws IOException {
        delegate().startOrderedMapNode(mapEntryType, childSizeHint);
    }

    @Override
    public void startMapEntryNode(final Identifier<?> keyValues, final int childSizeHint) throws IOException {
        delegate().startMapEntryNode(keyValues, childSizeHint);
    }

    @Override
    public void startChoiceNode(final Class<? extends DataContainer> choice, final int childSizeHint)
            throws IOException {
        delegate().startChoiceNode(choice, childSizeHint);
    }

    @Override
    public void startCase(final Class<? extends DataObject> caze, final int childSizeHint) throws IOException {
        delegate().startCase(caze, childSizeHint);
    }

    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType) throws IOException {
        delegate().startAugmentationNode(augmentationType);
    }

    @Override
    public void anyxmlNode(final String name, final Object value) throws IOException {
        delegate().anyxmlNode(name, value);
    }

    @Override
    public void endNode() throws IOException {
        delegate().endNode();
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }
}
