/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class AnydataSerializeTest extends AbstractAnydataTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private final XMLOutputFactory factory;

    public AnydataSerializeTest(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @Test
    public void testDOMAnydata() throws XMLStreamException, IOException, SAXException {
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
            xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
            xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(ImmutableAnydataNodeBuilder.create(DOMSource.class).withNodeIdentifier(FOO_NODEID)
            .withValue(toDOMSource("<bar/>")).build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<foo xmlns=\"test-anydata\"><bar></bar></foo>", serializedXml);
    }
}
