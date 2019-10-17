/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.Serializable;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class YangCommonSerializers extends Serializers.Base implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
            final BeanDescription beanDesc) {
        final Class<?> raw = type.getRawClass();
        if (Uint8.class.isAssignableFrom(raw) || Uint16.class.isAssignableFrom(raw)
                || Uint32.class.isAssignableFrom(raw) || Uint64.class.isAssignableFrom(raw)) {
            return ToStringSerializer.instance;
        }
        return null;
    }
}
