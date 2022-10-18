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
import org.opendaylight.yangtools.yang.common.YangVersion;

public class YangVersionSerializerTest {

    @Test
    public void testSerialize() {
        YangVersion yangVersion = YangVersion.VERSION_1_1;
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(YangVersion.class, new YangVersionSerializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"1.1\"", objectMapper.writeValueAsString(yangVersion));
        } catch (JsonProcessingException e) {
            assertNotNull("objectMapper.writeValueAsString() throws JsonProcessingException on a YangVersion object",
                    null);
        }
    }

}
