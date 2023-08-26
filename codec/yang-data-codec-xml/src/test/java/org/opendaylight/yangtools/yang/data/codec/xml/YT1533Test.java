/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1533Test {
    @Test
    void testInvalidChild() throws Exception {
        final var context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
            }""");
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, SchemaInferenceStack.of(context).toInference());

        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(UntrustedXML.createXMLStreamReader(
            new StringReader("<invalid xmlns=\"foo\"/>")))
        );
        assertEquals(ImmutableNodes.leafNode(QName.create("foo", "foo"), "  "), result.getResult().data());
    }
}
