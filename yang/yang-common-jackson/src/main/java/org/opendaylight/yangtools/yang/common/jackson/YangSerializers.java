/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.jackson.ser.EmptySerializer;

@Beta
public final class YangSerializers extends Serializers.Base {
    @Override
    public JsonSerializer<?> findReferenceSerializer(final SerializationConfig config, final ReferenceType type,
            final BeanDescription beanDesc, final TypeSerializer contentTypeSerializer,
            final JsonSerializer<Object> contentValueSerializer) {
        final Class<?> raw = type.getRawClass();
        if (Empty.class.isAssignableFrom(raw)) {
            return new EmptySerializer();
        } else if (Uint8.class.isAssignableFrom(raw)) {
            return new Uint8Serializer();
        } else {
            // ... Many more
        }


        return null;
    }
}
