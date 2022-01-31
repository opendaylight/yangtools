/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT994Test {
    @Test
    public void testAnydataParsing() {
        final var context = YangParserTestUtils.parseYangResource("/yt994.yang");
        final var result = new NormalizedNodeResult();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.RFC7951.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader("{\n"
            + "  \"foo:foo\": {"
            + "    \"bar:ba\"r : \"a string\"\n"
            + "  }\n"
            + "}")));
        final var data = result.getResult();
        assertThat(data, instanceOf(AnydataNode.class));
    }
}
