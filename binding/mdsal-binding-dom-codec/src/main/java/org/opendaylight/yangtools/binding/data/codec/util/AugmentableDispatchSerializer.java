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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Dispatch serializer, which emits {@link BindingStreamEventWriter#startAugmentationNode(Class)}
 * events for supplied augmentation node.
 *
 */
public class AugmentableDispatchSerializer implements DataObjectSerializerImplementation {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentableDispatchSerializer.class);

    @Override
    public void serialize(final DataObjectSerializerRegistry reg, final DataObject obj,
            final BindingStreamEventWriter stream) throws IOException {
        if (obj instanceof Augmentable<?>) {
            final Map<Class<? extends Augmentation<?>>, Augmentation<?>> augmentations;
            if (reg instanceof AugmentationReader) {
                augmentations = ((AugmentationReader) reg).getAugmentations(obj);
            } else if (Proxy.isProxyClass(obj.getClass())) {
                augmentations = getFromProxy(obj);
            } else {
                augmentations = BindingReflections.getAugmentations((Augmentable<?>) obj);
            }
            for (final Entry<Class<? extends Augmentation<?>>, Augmentation<?>> aug : augmentations.entrySet()) {
                emitAugmentation(aug.getKey(), aug.getValue(), stream, reg);
            }
        }
    }

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getFromProxy(final DataObject obj) {
        final InvocationHandler proxy = Proxy.getInvocationHandler(obj);
        if (proxy instanceof AugmentationReader) {
            return ((AugmentationReader) proxy).getAugmentations(obj);
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("rawtypes")
    private static void emitAugmentation(final Class type, final Augmentation<?> value,
            final BindingStreamEventWriter stream, final DataObjectSerializerRegistry registry) throws IOException {
        /*
         * Binding Specification allowed to insert augmentation with null for
         * value, which effectively could be used to remove augmentation
         * from builder / DTO.
         */
        if(value == null) {
            return;
        }
        Preconditions.checkArgument(value instanceof DataObject);
        @SuppressWarnings("unchecked")
        final DataObjectSerializer serializer = registry.getSerializer(type);
        if (serializer != null) {
            serializer.serialize((DataObject) value, stream);
        } else {
            LOG.warn("DataObjectSerializer is not present for {} in registry {}", type, registry);
        }
    }
}