/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.deser.Decimal64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.EmptyDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.RevisionDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint16Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint32Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint8Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.YangVersionDeserializer;

public final class YangDeserializers extends SimpleDeserializers {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(final ReferenceType refType,
            final DeserializationConfig config, final BeanDescription beanDesc,
            final TypeDeserializer contentTypeDeserializer, final JsonDeserializer<?> contentDeserializer)
                throws JsonMappingException {
        if (refType.hasRawClass(Empty.class)) {
            return new EmptyDeserializer();
        } else if (refType.hasRawClass(Uint8.class)) {
            return new Uint8Deserializer();
        } else if (refType.hasRawClass(Uint16.class)) {
            return new Uint16Deserializer();
        } else if (refType.hasRawClass(Uint32.class)) {
            return new Uint32Deserializer();
        } else if (refType.hasRawClass(Uint64.class)) {
            return new Uint64Deserializer();
        } else if (refType.hasRawClass(Decimal64.class)) {
            return new Decimal64Deserializer();
        } else if (refType.hasRawClass(Revision.class)) {
            return new RevisionDeserializer();
        } else if (refType.hasRawClass(YangVersion.class)) {
            return new YangVersionDeserializer();
        } else {
            return null;
        }
    }
}
