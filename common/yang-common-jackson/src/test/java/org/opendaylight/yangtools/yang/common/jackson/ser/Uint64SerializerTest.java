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
import org.opendaylight.yangtools.yang.common.Uint64;

public class Uint64SerializerTest {

    @Test
    public void testSerialize() {
        final Uint64 uint64 = Uint64.saturatedOf(168);
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Uint64.class, new Uint64Serializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"168\"", objectMapper.writeValueAsString(uint64));
        } catch (final JsonProcessingException e) {
            assertNotNull(
                    "objectMapper.writeValueAsString() throws JsonProcessingException on a Uint64 object",
                    null);
        }
    }

}
