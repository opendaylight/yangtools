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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataListBuilder;

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
    public void testOpaqueAnydata() throws XMLStreamException, IOException {
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
            xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
            xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(Builders.opaqueAnydataBuilder().withNodeIdentifier(FOO_NODEID)
            .withValue(new OpaqueDataBuilder().withAccurateLists(false)
                .withRoot(
                    new OpaqueDataListBuilder().withIdentifier(BAR_ID)
                    .withChild(new OpaqueDataContainerBuilder().withIdentifier(BAR_ID).build())
                    .build())
                .build())
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<foo xmlns=\"test-anydata\"><bar/></foo>", serializedXml);
    }
}
