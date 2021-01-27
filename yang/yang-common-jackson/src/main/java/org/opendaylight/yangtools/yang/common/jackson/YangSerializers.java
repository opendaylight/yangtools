/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.ser.Decimal64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.EmptySerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.RevisionSerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint16Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint32Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint8Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.YangVersionSerializer;

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
        } else if (Uint16.class.isAssignableFrom(raw)) {
            return new Uint16Serializer();
        } else if (Uint32.class.isAssignableFrom(raw)) {
            return new Uint32Serializer();
        } else if (Uint64.class.isAssignableFrom(raw)) {
            return new Uint64Serializer();
        } else if (Decimal64.class.isAssignableFrom(raw)) {
            return new Decimal64Serializer();
        } else if (Revision.class.isAssignableFrom(raw)) {
            return new RevisionSerializer();
        } else if (YangVersion.class.isAssignableFrom(raw)) {
            return new YangVersionSerializer();
        } else {
            return null;
        }
    }
}
