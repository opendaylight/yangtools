/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangErrorInfo;
import org.opendaylight.yangtools.yang.data.api.codec.YangMissingKeyException;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class YT1307Test extends AbstractComplexJsonTest {
    @Test
    void testMissingKey() {
        final var jsonParser = JsonParserStream.create(ImmutableNormalizedNodeStreamWriter.from(
            new NormalizationResultHolder()), lhotkaCodecFactory);

        final var ex = assertThrows(YangMissingKeyException.class,
            () -> jsonParser.parse(new JsonReader(new StringReader("""
                {
                  "complexjson:cont1": {
                    "lst11": [{
                      "key111": "value"
                    }]
                  }
                }"""))));
        assertEquals("""
            List entry is missing keys [YangErrorInfo{value=ImmutableLeafNode{\
            name=(urn:ietf:params:xml:ns:netconf:base:1.0?revision=2011-06-01)bad-element, \
            body=(ns:complex:json?revision=2014-08-11)lf111}}]""", ex.getMessage());
        final var errors = ex.getNetconfErrors();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.MISSING_ELEMENT, error.tag());
        assertNull(error.message());
        assertNull(error.appTag());
        assertNull(error.path());

        assertEquals(List.of(YangErrorInfo.of(ImmutableNodes.leafNode(
            QName.create("urn:ietf:params:xml:ns:netconf:base:1.0", "2011-06-01", "bad-element"),
            QName.create("ns:complex:json", "2014-08-11", "lf111")))), error.info());
    }
}
