/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.google.common.annotations.Beta;

@Beta
public final class YangDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findReferenceDeserializer(final ReferenceType refType,
            final DeserializationConfig config, final BeanDescription beanDesc,
            final TypeDeserializer contentTypeDeserializer, final JsonDeserializer<?> contentDeserializer)
                throws JsonMappingException {


        return null;
    }
}
