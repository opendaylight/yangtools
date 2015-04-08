package org.opendaylight.yangtools.binding.data.codec.impl;

import java.io.IOException;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

abstract class ForwardingBindingStreamEventWriter implements BindingStreamEventWriter {

    protected abstract BindingStreamEventWriter delegate();

    @Override
    public void leafNode(final String localName, final Object value) throws IOException, IllegalArgumentException {
        delegate().leafNode(localName, value);
    }


    @Override
    public void startLeafSet(final String localName, final int childSizeHint) throws IOException, IllegalArgumentException {
        delegate().startLeafSet(localName, childSizeHint);
    }


    @Override
    public void leafSetEntryNode(final Object value) throws IOException, IllegalArgumentException {
        delegate().leafSetEntryNode(value);
    }


    @Override
    public void startContainerNode(final Class<? extends DataObject> container, final int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startContainerNode(container, childSizeHint);
    }


    @Override
    public void startUnkeyedList(final Class<? extends DataObject> localName, final int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startUnkeyedList(localName, childSizeHint);
    }


    @Override
    public void startUnkeyedListItem(final int childSizeHint) throws IOException, IllegalStateException {
        delegate().startUnkeyedListItem(childSizeHint);
    }


    @Override
    public <T extends DataObject & Identifiable<?>> void startMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IOException, IllegalArgumentException {
        delegate().startMapNode(mapEntryType, childSizeHint);
    }


    @Override
    public <T extends DataObject & Identifiable<?>> void startOrderedMapNode(final Class<T> mapEntryType, final int childSizeHint)
            throws IOException, IllegalArgumentException {
        delegate().startOrderedMapNode(mapEntryType, childSizeHint);
    }


    @Override
    public void startMapEntryNode(final Identifier<?> keyValues, final int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startMapEntryNode(keyValues, childSizeHint);
    }


    @Override
    public void startChoiceNode(final Class<? extends DataContainer> choice, final int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startChoiceNode(choice, childSizeHint);
    }


    @Override
    public void startCase(final Class<? extends DataObject> caze, final int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startCase(caze, childSizeHint);
    }


    @Override
    public void startAugmentationNode(final Class<? extends Augmentation<?>> augmentationType) throws IOException,
            IllegalArgumentException {
        delegate().startAugmentationNode(augmentationType);
    }


    @Override
    public void anyxmlNode(final String name, final Object value) throws IOException, IllegalArgumentException {
        delegate().anyxmlNode(name, value);
    }


    @Override
    public void endNode() throws IOException, IllegalStateException {
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
