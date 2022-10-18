/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.deser;

import static  org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.YangVersion;

public class YangVersionDeserializerTest {

    @Test
    public void testDeserialize() throws JsonProcessingException {
        String yangVersion11String = "\"1.1\"";
        String yangVersion1String = "1";

        SimpleModule simpleModule = new SimpleModule().addDeserializer(YangVersion.class,
                new YangVersionDeserializer());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(simpleModule);

        assertEquals(YangVersion.VERSION_1_1, objectMapper.readValue(yangVersion11String, YangVersion.class));
        assertEquals(YangVersion.VERSION_1, objectMapper.readValue(yangVersion1String, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1_1, objectMapper.readValue(yangVersion1String, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1, objectMapper.readValue(yangVersion11String, YangVersion.class));
    }

}
