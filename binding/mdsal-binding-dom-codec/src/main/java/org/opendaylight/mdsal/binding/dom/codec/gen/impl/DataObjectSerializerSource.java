/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.dom.codec.gen.spi.AbstractSource;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;

abstract class DataObjectSerializerSource extends AbstractSource {

    private static final ClassLoadingStrategy STRATEGY = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    protected static final String SERIALIZER = "_serializer";
    protected static final String STREAM = "_stream";
    protected static final String ITERATOR = "_iterator";
    protected static final String CURRENT = "_current";
    protected static final String REGISTRY = "_registry";

    private final AbstractGenerator generator;

    /**
     * Create a new source.
     *
     * @param generator Parent generator
     */
    DataObjectSerializerSource(final AbstractGenerator generator) {
        this.generator = requireNonNull(generator);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends DataContainer> loadClass(final Type childType) {
        try {
            return (Class<? extends DataContainer>) STRATEGY.loadClass(childType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Could not load referenced class ", e);
        }
    }

    /**
     * Returns body of static serialize method.
     *
     * <ul>
     * <li> {@link DataObjectSerializerRegistry} - registry of serializers
     * <li> {@link DataObject} - object to be serialized
     * <li> {@link BindingStreamEventWriter} - writer to which events should be serialized.
     * </ul>
     *
     * @return Valid javassist code describing static serialization body.
     */
    protected abstract CharSequence getSerializerBody();

    protected static final CharSequence leafNode(final String localName, final CharSequence value) {
        return invoke(STREAM, "leafNode", escape(localName), value);
    }

    protected static final CharSequence startLeafSet(final String localName,final CharSequence expected) {
        return invoke(STREAM, "startLeafSet", escape(localName), expected);
    }

    protected static final CharSequence startOrderedLeafSet(final String localName, final CharSequence expected) {
        return invoke(STREAM, "startOrderedLeafSet", escape(localName), expected);
    }

    protected static final CharSequence leafSetEntryNode(final CharSequence value) {
        return invoke(STREAM, "leafSetEntryNode", value);
    }

    protected static final CharSequence startContainerNode(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startContainerNode", type, expected);
    }

    protected static final CharSequence escape(final String localName) {
        return '"' + localName + '"';
    }

    protected static final CharSequence startUnkeyedList(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedList", type, expected);
    }

    protected static final CharSequence startUnkeyedListItem(final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedListItem", expected);
    }

    protected static final CharSequence startMapNode(final CharSequence type,final CharSequence expected) {
        return invoke(STREAM, "startMapNode", type, expected);
    }

    protected static final CharSequence startOrderedMapNode(final CharSequence type,final CharSequence expected) {
        return invoke(STREAM, "startOrderedMapNode", type, expected);
    }

    protected static final CharSequence startMapEntryNode(final CharSequence key, final CharSequence expected) {
        return invoke(STREAM, "startMapEntryNode", key, expected);
    }

    protected static final CharSequence startAugmentationNode(final CharSequence key) {
        return invoke(STREAM, "startAugmentationNode", key);
    }

    protected static final CharSequence startChoiceNode(final CharSequence localName,final CharSequence expected) {
        return invoke(STREAM, "startChoiceNode", localName, expected);
    }

    protected static final CharSequence startCaseNode(final CharSequence localName,final CharSequence expected) {
        return invoke(STREAM, "startCase", localName, expected);
    }

    protected static final CharSequence anyxmlNode(final String name, final String value)
            throws IllegalArgumentException {
        return invoke(STREAM, "anyxmlNode", escape(name), name);
    }

    protected static final CharSequence endNode() {
        return invoke(STREAM, "endNode");
    }

    protected static final CharSequence forEach(final String iterable,final Type valueType,final CharSequence body) {
        return forEach(iterable, ITERATOR, valueType.getFullyQualifiedName(), CURRENT, body);
    }

    protected static final CharSequence classReference(final Type type) {
        return type.getFullyQualifiedName() + ".class";
    }

    protected final CharSequence staticInvokeEmitter(final Type childType, final String name) {
        final Class<?> cls;
        try {
            cls = STRATEGY.loadClass(childType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Failed to invoke emitter", e);
        }

        final String className = this.generator.loadSerializerFor(cls) + ".getInstance()";
        return invoke(className, AbstractStreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, name, STREAM);
    }
}
