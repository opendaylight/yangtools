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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataContainerBuilder;
import org.opendaylight.yangtools.yang.data.util.schema.opaque.OpaqueDataListBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(Parameterized.class)
public class AnydataSerializeTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private static final QName FOO_QNAME = QName.create("test-anydata", "foo");
    private static final QName BAR_QNAME = QName.create(FOO_QNAME, "bar");
    private static final NodeIdentifier FOO_NODEID = NodeIdentifier.create(FOO_QNAME);
    private static final NodeIdentifier BAR_NODEID = NodeIdentifier.create(BAR_QNAME);

    private static SchemaContext SCHEMA_CONTEXT;

    private final XMLOutputFactory factory;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/test-anydata.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

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
                    new OpaqueDataListBuilder().withIdentifier(BAR_NODEID)
                    .withChild(new OpaqueDataContainerBuilder().withIdentifier(BAR_NODEID).build())
                    .build())
                .build())
            .build());
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        assertEquals("<foo xmlns=\"test-anydata\"><bar/></foo>", serializedXml);
    }
}
