/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.util;

import com.google.common.base.Preconditions;

import java.io.IOException;

import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChoiceDispatchSerializer implements DataObjectSerializerImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(ChoiceDispatchSerializer.class);

    @SuppressWarnings("rawtypes")
    private final Class choiceClass;

    @SuppressWarnings("rawtypes")
    private ChoiceDispatchSerializer(final Class choiceClass) {
        this.choiceClass = Preconditions.checkNotNull(choiceClass);
    }

    public static final ChoiceDispatchSerializer from(final Class<? extends DataContainer> choiceClass) {
        return new ChoiceDispatchSerializer(choiceClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj, final BindingStreamEventWriter stream) throws IOException {
        @SuppressWarnings("rawtypes")
        Class cazeClass = obj.getImplementedInterface();
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
