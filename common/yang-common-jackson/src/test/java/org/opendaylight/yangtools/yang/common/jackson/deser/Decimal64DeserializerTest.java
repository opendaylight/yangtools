/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

public class Decimal64DeserializerTest {

    @Test
    public void testDeserialize() {
        final String correctDecimal64String = "\"255.0\"";
        final String correctDecimal64String2 = "255.0";

        final SimpleModule simpleModule = new SimpleModule().addDeserializer(Decimal64.class,
                new Decimal64Deserializer());
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        try {
            assertEquals("255.0",
                    objectMapper.readValue(correctDecimal64String, Decimal64.class).toString());
            assertEquals("255.0",
                    objectMapper.readValue(correctDecimal64String2, Decimal64.class).toString());
        } catch (final JsonProcessingException e) {
            assertNull(
                    "objectMapper.readValue() throws JsonProcessingException while deserialising to Decimal64",
                    null);
        }
    }

}
