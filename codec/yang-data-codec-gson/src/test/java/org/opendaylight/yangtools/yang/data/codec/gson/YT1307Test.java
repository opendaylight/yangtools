/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.YangMissingKeyException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

public class YT1307Test extends AbstractComplexJsonTest {
    @Test
    public void testMissingKey() throws IOException {
        final JsonParserStream jsonParser = JsonParserStream.create(
            ImmutableNormalizedNodeStreamWriter.from(new NormalizedNodeResult()), lhotkaCodecFactory);

        final YangMissingKeyException ex = assertThrows(YangMissingKeyException.class,
            () -> jsonParser.parse(new JsonReader(new StringReader("{\n"
                + "  \"complexjson:cont1\": {\n"
                + "    \"lst11\": [{\n"
                + "      \"key111\": \"value\"\n"
                + "    }]\n"
                + "  }\n"
                + "}"))));

        assertEquals(Set.of(QName.create("ns:complex:json", "2014-08-11", "lf111")), ex.getMissingKeys());
    }
}
