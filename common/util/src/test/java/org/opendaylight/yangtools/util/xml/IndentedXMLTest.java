/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import org.junit.jupiter.api.Test;

class IndentedXMLTest {
    @Test
    void testFactories() {
        final var def = IndentedXML.of();
        assertSame(def, IndentedXML.of());
        assertSame(IndentedXML.of(), IndentedXML.of(2));
        assertEquals("IndentedXML{indentSize=2}", def.toString());

        final var four = IndentedXML.of(4);
        assertNotSame(IndentedXML.of(), four);
        assertEquals("IndentedXML{indentSize=4}", four.toString());
    }

    @Test
    void testIndent() throws Exception {
        final var stream = new StringWriter();
        final var writer = XMLOutputFactory.newDefaultFactory().createXMLStreamWriter(stream);
        IndentedXML.of().writeIndent(writer, 44);
        writer.close();

        assertEquals(
            "\n                                                                                        ",
            stream.toString());
    }
}
