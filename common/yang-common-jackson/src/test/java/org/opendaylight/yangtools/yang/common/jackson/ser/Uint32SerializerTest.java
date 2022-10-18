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
import org.opendaylight.yangtools.yang.common.Uint32;

public class Uint32SerializerTest {

    @Test
    public void testSerialize() {
        final Uint32 uint32 = Uint32.saturatedOf(168);
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Uint32.class, new Uint32Serializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("168", objectMapper.writeValueAsString(uint32));
        } catch (final JsonProcessingException e) {
            assertNotNull(
                    "objectMapper.writeValueAsString() throws JsonProcessingException on a Uint32 object",
                    null);
        }
    }

}
