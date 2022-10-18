/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.ser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Empty;

public class EmptySerializerTest {

    @Test
    public void testSerialize() {
        final Empty empty = Empty.value();
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Empty.class, new EmptySerializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"[null]\"", objectMapper.writeValueAsString(empty));
        } catch (final JsonProcessingException e) {
            assertNotNull(
                    "objectMapper.writeValueAsString() throws JsonProcessingException on an Empty object",
                    null);
        }

    }

}
