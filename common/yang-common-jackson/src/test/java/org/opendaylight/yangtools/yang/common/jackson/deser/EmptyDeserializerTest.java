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
import org.opendaylight.yangtools.yang.common.Empty;

public class EmptyDeserializerTest {

    @Test
    public void testDeserialize() {
        final String emptyString = "[null]";

        final SimpleModule simpleModule = new SimpleModule().addDeserializer(Empty.class,
                new EmptyDeserializer());
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        try {
            assertEquals("empty", objectMapper.readValue(emptyString, Empty.class).toString());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
