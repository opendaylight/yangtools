/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.gen.impl;

import com.google.common.base.Preconditions;

import org.opendaylight.yangtools.binding.data.codec.gen.spi.AbstractSource;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;

abstract class DataObjectSerializerSource extends AbstractSource {

    private static final ClassLoadingStrategy STRATEGY = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    protected static final String STREAM = "_stream";
    protected static final String ITERATOR = "_iterator";
    protected static final String CURRENT = "_current";
    protected static final String REGISTRY = "_registry";

    private final AbstractGenerator generator;

    /**
     * @param generator Parent generator
     */
    DataObjectSerializerSource(final AbstractGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator);
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends DataContainer> loadClass(final Type childType) {
        try {
            return (Class<? extends DataContainer>) STRATEGY.loadClass(childType);
        } catch (ClassNotFoundException e) {
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

    protected final CharSequence leafNode(final String localName, final CharSequence value) {
        return invoke(STREAM, "leafNode", escape(localName), value);
    }

    protected final CharSequence startLeafSet(final String localName,final CharSequence expected) {
        return invoke(STREAM, "startLeafSet", escape(localName),expected);
    }

    protected final CharSequence startOrderedLeafSet(final String localName,final CharSequence expected) {
        return invoke(STREAM, "startOrderedLeafSet", escape(localName),expected);
    }


    protected final CharSequence leafSetEntryNode(final CharSequence value) {
        return invoke(STREAM, "leafSetEntryNode", value);

    }

    protected final CharSequence startContainerNode(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startContainerNode", (type),expected);
    }

    protected final  CharSequence escape(final String localName) {
        return '"'+localName+'"';
    }

    protected final CharSequence startUnkeyedList(final CharSequence type, final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedList", (type),expected);
    }

    protected final CharSequence startUnkeyedListItem(final CharSequence expected) {
        return invoke(STREAM, "startUnkeyedListItem",expected);
    }

    protected final CharSequence startMapNode(final CharSequence type,final CharSequence expected) {
        return invoke(STREAM, "startMapNode", (type),expected);
    }

    protected final CharSequence startOrderedMapNode(final CharSequence type,final CharSequence expected) {
        return invoke(STREAM, "startOrderedMapNode", (type),expected);
    }

    protected final CharSequence startMapEntryNode(final CharSequence key, final CharSequence expected) {
        return invoke(STREAM,"startMapEntryNode",key,expected);

    }

    protected final CharSequence startAugmentationNode(final CharSequence key) {
        return invoke(STREAM,"startAugmentationNode",key);

    }

    protected final CharSequence startChoiceNode(final CharSequence localName,final CharSequence expected) {
        return invoke(STREAM, "startChoiceNode", (localName),expected);
    }

    protected final CharSequence startCaseNode(final CharSequence localName,final CharSequence expected) {
        return invoke(STREAM, "startCase", (localName),expected);
    }


    protected final CharSequence anyxmlNode(final String name, final String value) throws IllegalArgumentException {
        return invoke(STREAM, "anyxmlNode", escape(name),name);
    }

    protected final CharSequence endNode() {
        return invoke(STREAM, "endNode");
    }

    protected final CharSequence forEach(final String iterable,final Type valueType,final CharSequence body) {
        return forEach(iterable,ITERATOR,valueType.getFullyQualifiedName(),CURRENT,body);
    }

    protected final CharSequence classReference(final Type type) {
        return new StringBuilder().append(type.getFullyQualifiedName()).append(".class");
    }

    protected final CharSequence staticInvokeEmitter(final Type childType, final String name) {
        final Class<?> cls;
        try {
            cls = STRATEGY.loadClass(childType);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to invoke emitter", e);
        }

        String className = this.generator.loadSerializerFor(cls) + ".getInstance()";
        return invoke(className, AbstractStreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, name, STREAM);
    }
}