/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

public class YT1029Test extends AbstractComplexJsonTest {
    @Test
    public void testMultipleRootChildren() throws IOException {
        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream)) {
            nodeWriter.write(CONT1_WITH_EMPTYLEAF);
            nodeWriter.write(CONT1_WITH_EMPTYLEAF);
        }

        assertEquals("{\n"
                + "  \"complexjson:cont1\": {\n"
                + "    \"empty\": [\n"
                + "      null\n"
                + "    ]\n"
                + "  },\n"
                + "  \"complexjson:cont1\": {\n"
                + "    \"empty\": [\n"
                + "      null\n"
                + "    ]\n"
                + "  }\n"
                + "}", writer.toString());
    }
}
