/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

class YT1029Test extends AbstractComplexJsonTest {
    @Test
    void testMultipleRootChildren() throws Exception {
        final var writer = new StringWriter();
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));
        try (var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(CONT1_WITH_EMPTYLEAF);
            nodeWriter.write(CONT1_WITH_EMPTYLEAF);
        }

        assertEquals("""
            {
              "complexjson:cont1": {
                "empty": [
                  null
                ]
              },
              "complexjson:cont1": {
                "empty": [
                  null
                ]
              }
            }""", writer.toString());
    }
}
