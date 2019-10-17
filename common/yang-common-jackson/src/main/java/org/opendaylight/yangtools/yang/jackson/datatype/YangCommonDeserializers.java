/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import java.io.Serializable;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class YangCommonDeserializers extends Deserializers.Base implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, final DeserializationConfig config,
            final BeanDescription beanDesc) {
        if (type.hasRawClass(Uint8.class)) {
            return Uint8Deserializer.INSTANCE;
        }
        if (type.hasRawClass(Uint16.class)) {
            return Uint16Deserializer.INSTANCE;
        }
        if (type.hasRawClass(Uint32.class)) {
            return Uint32Deserializer.INSTANCE;
        }
        if (type.hasRawClass(Uint64.class)) {
            return Uint64Deserializer.INSTANCE;
        }
        return null;
    }
}
