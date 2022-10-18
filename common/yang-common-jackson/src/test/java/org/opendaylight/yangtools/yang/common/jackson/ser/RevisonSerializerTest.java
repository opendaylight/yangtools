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
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;

public class RevisonSerializerTest {

    @Test
    public void testSerialize() {
        Revision revision = Revision.of("2022-10-18");
        Optional<Revision> nonEmptyRevision = Revision.ofNullable("2022-10-18");
        Optional<Revision> emptyRevision = Revision.ofNullable(null);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Revision.class, new RevisionSerializer());
        objectMapper.registerModule(simpleModule);

        try {
            assertEquals("\"2022-10-18\"", objectMapper.writeValueAsString(revision));
            assertEquals("\"2022-10-18\"", objectMapper.writeValueAsString(nonEmptyRevision.get()));
            assertThrows(java.util.NoSuchElementException.class,
                    () -> objectMapper.writeValueAsString(emptyRevision.get()));
        } catch (JsonProcessingException e) {
            assertNotNull("objectMapper.writeValueAsString() throws JsonProcessingException on a Revision object",
                    null);
        }

    }
}
