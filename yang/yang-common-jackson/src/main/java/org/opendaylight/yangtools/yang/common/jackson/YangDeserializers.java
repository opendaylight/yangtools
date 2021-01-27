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

public final class YangDeserializers extends SimpleDeserializers {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(final ReferenceType refType,
            final DeserializationConfig config, final BeanDescription beanDesc,
            final TypeDeserializer contentTypeDeserializer, final JsonDeserializer<?> contentDeserializer)
                throws JsonMappingException {


        return null;
    }
}
