/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1533Test {
    @Test
    void testInvalidChild() throws Exception {
        final var context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;

              container foo {
                container bar;
              }
            }""");
        final var result = new NormalizedNodeResult();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter, SchemaInferenceStack.of(context).toInference());

        final var ex = assertThrows(XMLStreamException.class,
            () -> xmlParser.parse(UntrustedXML.createXMLStreamReader(new StringReader("""
                <foo xmlns="foo">
                  <bar/>
                </foo>""")))
        );
        assertThat(ex.getMessage(), startsWith("""
            ParseError at [row,col]:[2,9]
            Message: Schema for node with name bar and namespace foo does not exist in parent EffectiveSchema"""));
    }
}
