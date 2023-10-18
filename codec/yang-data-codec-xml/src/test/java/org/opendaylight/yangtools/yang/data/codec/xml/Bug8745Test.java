/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertFalse;

import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;

@RunWith(Parameterized.class)
public class Bug8745Test extends AbstractXmlTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private static EffectiveModelContext SCHEMA_CONTEXT;

    private final XMLOutputFactory factory;

    public Bug8745Test(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;

              container cont-with-attributes {
                leaf leaf-with-attributes {
                  type string;
                }
                leaf-list leaf-list-with-attributes {
                  type string;
                }
                list list-with-attributes {
                  key list-key;
                  leaf list-key {
                    type string;
                  }
                }
              }
            }""");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Test
    public void testParsingAttributes() throws Exception {
        final var doc = loadDocument("/bug8745/foo.xml");
        final var domSource = new DOMSource(doc.getDocumentElement());
        final var domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        final var xmlStreamWriter = factory.createXMLStreamWriter(domResult);
        final var streamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter, SCHEMA_CONTEXT);

        final var reader = new DOMSourceXMLStreamReader(domSource);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(SCHEMA_CONTEXT, QName.create("foo", "cont-with-attributes")));
        xmlParser.parse(reader);

        final var diff = DiffBuilder.compare(toString(doc.getDocumentElement()))
            .withTest(toString(domResult.getNode()))
            .ignoreWhitespace()
            .checkForIdentical()
            .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }
}
