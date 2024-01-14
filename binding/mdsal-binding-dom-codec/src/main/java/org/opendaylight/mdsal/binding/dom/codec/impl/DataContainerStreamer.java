/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base superclass for all concrete streamers, that is objects which are able to turn a concrete DataObject into a
 * stream of events.
 *
 * @param <T> DataContainer type
 */
@Beta
public abstract class DataContainerStreamer<T extends DataContainer> {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerStreamer.class);

    protected DataContainerStreamer() {

    }

    /**
     * Writes stream events for supplied data object to provided stream.
     *
     * <p>
     * DataObjectSerializerRegistry may be used to lookup serializers for other generated classes  in order to support
     * writing their events.
     */
    protected abstract void serialize(DataContainerSerializerRegistry reg, DataContainer obj,
        BindingStreamEventWriter stream) throws IOException;

    protected static final void streamAnydata(final BindingStreamEventWriter writer, final String localName,
            final Object value) throws IOException {
        if (value != null && writer instanceof AnydataBindingStreamWriter anydataWriter) {
            verify(value instanceof OpaqueObject, "Unexpected data %s", value);
            anydataWriter.anydataNode(localName, (OpaqueObject<?>) value);
        }
    }

    protected static final void streamAnyxml(final BindingStreamEventWriter writer, final String localName,
            final Object value) throws IOException {
        if (value != null) {
            writer.anyxmlNode(localName, value);
        }
    }

    protected static final void streamAugmentations(final DataContainerSerializerRegistry registry,
            final BindingStreamEventWriter writer, final Augmentable<?> obj) throws IOException {
        for (final var aug : obj.augmentations().entrySet()) {
            emitAugmentation(aug.getKey(), aug.getValue(), writer, registry);
        }
    }

    protected static final <C extends DataContainer> void streamChoice(final Class<C> choiceClass,
            final DataContainerSerializerRegistry registry, final BindingStreamEventWriter writer, final C value)
                    throws IOException {
        if (value != null) {
            final Class<? extends DataContainer> caseClass = value.implementedInterface();
            writer.startChoiceNode(choiceClass, BindingStreamEventWriter.UNKNOWN_SIZE);
            final DataContainerSerializer caseStreamer = registry.getSerializer(caseClass.asSubclass(DataObject.class));
            if (caseStreamer != null) {
                if (tryCache(writer, (DataObject) value)) {
                    caseStreamer.serialize(value, writer);
                }
            } else {
                LOG.warn("No serializer for case {} is available in registry {}", caseClass, registry);
            }

            writer.endNode();
        }
    }

    protected static final <C extends DataObject> void streamContainer(final DataContainerStreamer<C> childStreamer,
            final DataContainerSerializerRegistry registry, final BindingStreamEventWriter writer, final C value)
                    throws IOException {
        if (value != null && tryCache(writer, value)) {
            childStreamer.serialize(registry, value, writer);
        }
    }

    protected static final void streamLeaf(final BindingStreamEventWriter writer, final String localName,
            final Object value) throws IOException {
        if (value != null) {
            writer.leafNode(localName, value);
        }
    }

    protected static final void streamLeafList(final BindingStreamEventWriter writer, final String localName,
            final Set<?> value) throws IOException {
        if (value != null) {
            writer.startLeafSet(localName, value.size());
            commonStreamLeafset(writer, value);
        }
    }

    protected static final void streamOrderedLeafList(final BindingStreamEventWriter writer,
            final String localName, final List<?> value) throws IOException {
        if (value != null) {
            writer.startOrderedLeafSet(localName, value.size());
            commonStreamLeafset(writer, value);
        }
    }

    protected static final <E extends DataObject> void streamList(final Class<E> childClass,
            final DataContainerStreamer<E> childStreamer, final DataContainerSerializerRegistry registry,
            final BindingStreamEventWriter writer, final List<? extends E> value) throws IOException {
        final int size = nullSize(value);
        if (size != 0) {
            writer.startUnkeyedList(childClass, size);
            commonStreamList(registry, writer, childStreamer, value);
        }
    }

    protected static final <E extends DataObject & KeyAware<?>> void streamMap(final Class<E> childClass,
            final DataContainerStreamer<E> childStreamer, final DataContainerSerializerRegistry registry,
            final BindingStreamEventWriter writer, final Map<?, ? extends E> value) throws IOException {
        final int size = nullSize(value);
        if (size != 0) {
            writer.startMapNode(childClass, size);
            commonStreamList(registry, writer, childStreamer, value.values());
        }
    }

    protected static final <E extends DataObject & KeyAware<?>> void streamOrderedMap(final Class<E> childClass,
            final DataContainerStreamer<E> childStreamer, final DataContainerSerializerRegistry registry,
            final BindingStreamEventWriter writer, final List<? extends E> value) throws IOException {
        final int size = nullSize(value);
        if (size != 0) {
            writer.startOrderedMapNode(childClass, size);
            commonStreamList(registry, writer, childStreamer, value);
        }
    }

    private static <E extends DataObject> void commonStreamList(final DataContainerSerializerRegistry registry,
            final BindingStreamEventWriter writer, final DataContainerStreamer<E> childStreamer,
            final Collection<? extends E> value) throws IOException {

        for (E entry : value) {
            if (tryCache(writer, entry)) {
                childStreamer.serialize(registry, entry, writer);
            }
        }
        writer.endNode();
    }

    private static void commonStreamLeafset(final BindingStreamEventWriter writer, final Collection<?> value)
            throws IOException {
        for (Object entry : value) {
            writer.leafSetEntryNode(entry);
        }
        writer.endNode();
    }

    private static void emitAugmentation(final Class<? extends Augmentation<?>> type, final Augmentation<?> value,
            final BindingStreamEventWriter writer, final DataContainerSerializerRegistry registry) throws IOException {
        /*
         * Binding Specification allowed to insert augmentation with null for value, which effectively could be used to
         * remove augmentation from builder / DTO.
         */
        if (value != null) {
            final DataContainerSerializer serializer = registry.getSerializer(type);
            if (serializer != null) {
                serializer.serialize(value, writer);
            } else {
                LOG.warn("DataObjectSerializer is not present for {} in registry {}", type, registry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataObject> boolean tryCache(final BindingStreamEventWriter writer, final T value) {
        // Force serialization if writer is not a BindingSerializer, otherwise defer to it for a decision
        return !(writer instanceof BindingSerializer) || ((BindingSerializer<?, T>) writer).serialize(value) == null;
    }

    private static int nullSize(final Collection<?> list) {
        return list == null ? 0 : list.size();
    }

    private static int nullSize(final Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }
}
