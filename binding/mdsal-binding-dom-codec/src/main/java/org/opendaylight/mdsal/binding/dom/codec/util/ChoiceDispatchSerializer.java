/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChoiceDispatchSerializer implements DataObjectSerializerImplementation {
    private static final Logger LOG = LoggerFactory.getLogger(ChoiceDispatchSerializer.class);

    private final @NonNull Class<? extends DataContainer> choiceClass;

    private ChoiceDispatchSerializer(final Class<? extends DataContainer> choiceClass) {
        this.choiceClass = requireNonNull(choiceClass);
    }

    public static @NonNull ChoiceDispatchSerializer from(final Class<? extends DataContainer> choiceClass) {
        return new ChoiceDispatchSerializer(choiceClass);
    }

    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj,
            final BindingStreamEventWriter stream) throws IOException {
        Class<? extends DataObject> cazeClass = obj.implementedInterface();
        stream.startChoiceNode(choiceClass, BindingStreamEventWriter.UNKNOWN_SIZE);
        DataObjectSerializer caseSerializer = reg.getSerializer(cazeClass);
        if (caseSerializer != null) {
            caseSerializer.serialize(obj, stream);
        } else {
            LOG.warn("No serializer for case {} is available in registry {}", cazeClass, reg);
        }
        stream.endNode();
    }
}
