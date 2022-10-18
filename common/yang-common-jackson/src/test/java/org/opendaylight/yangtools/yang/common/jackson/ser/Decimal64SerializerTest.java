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
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;

public class Decimal64SerializerTest {

    @Test
    public void testSerialize() {
        final Decimal64 correctDecimal64 = Decimal64.valueOf("125");
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Decimal64.class, new Decimal64Serializer());
        mapper.registerModule(module);

        try {
            assertEquals("\"125.0\"", mapper.writeValueAsString(correctDecimal64));
        } catch (final JsonProcessingException e) {
            assertNotNull(null);
        }
        assertThrows(java.lang.NumberFormatException.class, () -> Decimal64.valueOf("auto"));

    }
}
