/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8675Test {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2017-06-13"));
    private static final String FOO_YANG = """
            module foo {
                namespace foo;
                prefix foo;
                revision 2017-06-13;
                list top-level-list {
                    key key-leaf;
                    leaf key-leaf {
                        type string;
                    }
                    leaf ordinary-leaf {
                        type string;
                    }
                    container cont-in-list {}
                    list inner-list {
                        key inner-key-leaf;
                        leaf inner-key-leaf {
                            type string;
                        }
                        leaf inner-ordinary-leaf {
                            type string;
                        }
                    }
                }
                container top-level-container {
                    container inner-container-1 {}
                    container inner-container-2 {}
                }
                anyxml top-level-anyxml;
                leaf top-level-leaf {
                    type int32;
                }
                leaf-list top-level-leaf-list {
                    type int32;
                }
            }""";

    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYang(FOO_YANG);
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
    }

    @Test
    public void testParsingEmptyElements() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-container")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testParsingEmptyRootElement() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo-2.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-container")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testListAsRootElement() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-3.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-list")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testAnyXmlAsRootElement() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-4.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-anyxml")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafAsRootElement() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-5.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-leaf")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafListAsRootElement() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-6.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-leaf-list")));
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
