/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import org.opendaylight.mdsal.binding.dom.codec.util.AugmentableDispatchSerializer;
import org.opendaylight.mdsal.binding.dom.codec.util.ChoiceDispatchSerializer;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;

/**
 * Concrete implementation of {@link AbstractStreamWriterGenerator} which in runtime generates classes implementing
 * {@link DataObjectSerializerImplementation} interface and are used to serialize Binding {@link DataObject}.
 *
 * <p>
 * Actual implementation of codecs is done via static methods, which allows for static wiring of codecs. Choice codec
 * and Augmentable codecs are static properties of parent codec and stateless implementations are used
 * ({@link ChoiceDispatchSerializer}, {@link AugmentableDispatchSerializer}), which uses registry to dispatch to
 * concrete item codec.
 */
public final class StreamWriterGenerator extends AbstractStreamWriterGenerator {
    private static final String UNKNOWN_SIZE = BindingStreamEventWriter.class.getName() + ".UNKNOWN_SIZE";

    private StreamWriterGenerator(final JavassistUtils utils) {
        super(utils);
    }

    /**
     * Create a new instance backed by a specific {@link JavassistUtils} instance.
     *
     * @param utils JavassistUtils instance to use
     * @return A new generator
     */
    public static DataObjectSerializerGenerator create(final JavassistUtils utils) {
        return new StreamWriterGenerator(utils);
    }

    @Override
    protected DataObjectSerializerSource generateContainerSerializer(final GeneratedType type,
            final ContainerSchemaNode node) {
        return new AugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startContainerNode(classReference(type), UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected DataObjectSerializerSource generateNotificationSerializer(final GeneratedType type,
            final NotificationDefinition node) {
        return new AugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startContainerNode(classReference(type), UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected DataObjectSerializerSource generateCaseSerializer(final GeneratedType type, final CaseSchemaNode node) {
        return new AugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startCaseNode(classReference(type),UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected DataObjectSerializerSource generateUnkeyedListEntrySerializer(final GeneratedType type,
            final ListSchemaNode node) {
        return new AugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startUnkeyedListItem(UNKNOWN_SIZE);
            }
        };
    }

    @Override
    protected DataObjectSerializerSource generateSerializer(final GeneratedType type,
            final AugmentationSchemaNode schema) {
        return new DataNodeContainerSerializerSource(this, type, schema) {
            @Override
            public CharSequence emitStartEvent() {
                return startAugmentationNode(classReference(type));
            }
        };
    }

    @Override
    protected DataObjectSerializerSource generateMapEntrySerializer(final GeneratedType type,
            final ListSchemaNode node) {
        return new AugmentableDataNodeContainerEmitterSource(this, type, node) {
            @Override
            public CharSequence emitStartEvent() {
                return startMapEntryNode(invoke(INPUT, BindingMapping.IDENTIFIABLE_KEY_NAME), UNKNOWN_SIZE);
            }
        };
    }
}
