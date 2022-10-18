/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.deser;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint16;

public class Uint16DeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        final String uint16String = "156";

        final SimpleModule simpleModule = new SimpleModule().addDeserializer(Uint16.class,
                new Uint16Deserializer());
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(156, objectMapper.readValue(uint16String, Uint16.class).intValue());
    }

}
