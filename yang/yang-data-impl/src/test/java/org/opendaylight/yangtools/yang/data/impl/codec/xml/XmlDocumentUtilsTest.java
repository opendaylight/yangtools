/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.mockito.Mockito.doReturn;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class XmlDocumentUtilsTest {
    private static final String NS = "urn:opendaylight:controller:xml:doc:test";
    private static final String NS2 = "urn:opendaylight:controller:xml:doc:test2";
    private static final String NS3 = "urn:opendaylight:controller:xml:doc:test3";
    private static final String REVISION = "2014-07-28";

    private static final String XML_CONTENT = "<input xmlns=\"urn:opendaylight:controller:xml:doc:test\">\n"
            + "<a>value</a>\n"
            + "<ref xmlns:ltha=\"urn:opendaylight:controller:xml:doc:test\">"
            + "/ltha:cont/ltha:l[  ltha:id='id/foo/bar'  ]"
            + "</ref>\n"
            + "</input>";

    private SchemaContext schema;

    @Before
    public void setUp() throws Exception {
        final File rpcTestYang1 = new File(getClass().getResource("xml-doc-test.yang").toURI());
        final File rpcTestYang2 = new File(getClass().getResource("xml-doc-test2.yang").toURI());

        this.schema = YangParserTestUtils.parseYangSources(rpcTestYang1, rpcTestYang2);
    }

    public static Document readXmlToDocument(final String xmlContent) throws SAXException, IOException {
        return readXmlToDocument(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
    }

    public static Document readXmlToDocument(final InputStream xmlContent) throws SAXException, IOException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    @Test
    public void xmlDocCreateElementForContWithoutAttrTest() throws Exception {
        final Document doc = readXmlToDocument(XML_CONTENT);
        final YangInstanceIdentifier.NodeIdentifier container = new YangInstanceIdentifier.NodeIdentifier(
                QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "cont"));

        final NormalizedNode<?, ?> data = ImmutableNodes.fromInstanceId(this.schema,
                YangInstanceIdentifier.create(container));
        Assert.assertNotNull(data);

        final Element element = XmlDocumentUtils.createElementFor(doc, data);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getTagName(), "cont");
    }

    @Test
    public void xmlDocCreateElementForContiWithAttrTest() throws Exception {
        final Document doc = readXmlToDocument(XML_CONTENT);
        final YangInstanceIdentifier.NodeIdentifier container = new YangInstanceIdentifier.NodeIdentifier(
                QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "cont"));

        final Map<QName, String> attributes = new HashMap<>();
        attributes.put(QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "l"), "list");

        final ContainerNode node = Builders.containerBuilder().withNodeIdentifier(container).withAttributes(attributes)
                .build();
        final NormalizedNode<?, ?> data = node;
        Assert.assertNotNull(data);

        final Element element = XmlDocumentUtils.createElementFor(doc, data);
        Assert.assertNotNull(element);
        Assert.assertEquals("cont", element.getTagName());
        Assert.assertEquals("list", element.getAttribute("l"));
    }

    @Test
    public void xmlDocgetModifyOperationWithAttributesTest() throws Exception {
        final Document doc = readXmlToDocument(XML_CONTENT);
        final YangInstanceIdentifier.NodeIdentifier container = new YangInstanceIdentifier.NodeIdentifier(
                QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "cont"));

        final Map<QName, String> attributes = new HashMap<>();
        attributes.put(QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "l"), "list");

        final ContainerNode node = Builders.containerBuilder().withNodeIdentifier(container).withAttributes(attributes)
                .build();
        final NormalizedNode<?, ?> data = node;
        Assert.assertNotNull(data);

        final Element element = XmlDocumentUtils.createElementFor(doc, data);
        final Optional<ModifyAction> modifyOperationFromAttributes = XmlDocumentUtils
                .getModifyOperationFromAttributes(element);
        Assert.assertFalse(modifyOperationFromAttributes.isPresent());
    }

    @Test
    public void xmlDocgetModifyOperationWithoutAttributesTest() {
        final Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttributeNS(Mockito.anyString(), Mockito.anyString())).thenReturn("hello");

        final Optional<ModifyAction> modifyOperationFromAttributes = XmlDocumentUtils
                .getModifyOperationFromAttributes(element);
        Assert.assertFalse(modifyOperationFromAttributes.isPresent());
    }

    @Test
    public void xmlDocFindFirstSchemaContTest() {
        final QName ID = QName.create(XmlDocumentUtilsTest.NS, XmlDocumentUtilsTest.REVISION, "cont");
        final Optional<DataSchemaNode> findFirstSchema = XmlDocumentUtils.findFirstSchema(ID,
                this.schema.getChildNodes());
        Assert.assertNotNull(findFirstSchema);
        Assert.assertEquals(ID, findFirstSchema.get().getQName());
    }

    @Test
    public void xmlDocFindFirstSchemaCont2Test() {
        final QName ID = QName.create(XmlDocumentUtilsTest.NS2, XmlDocumentUtilsTest.REVISION, "cont2");
        final Optional<DataSchemaNode> findFirstSchema = XmlDocumentUtils.findFirstSchema(ID,
                this.schema.getChildNodes());
        final DataSchemaNode dataSchemaNode = findFirstSchema.get();
        Assert.assertNotNull(findFirstSchema);
        Assert.assertEquals(ID, dataSchemaNode.getQName());
    }

    @Test
    public void xmlDocFindFirstSchemaNullParamsTest() {
        Optional<DataSchemaNode> findFirstSchema = XmlDocumentUtils.findFirstSchema(null,
                this.schema.getChildNodes());
        Assert.assertFalse(findFirstSchema.isPresent());

        final QName ID = QName.create(XmlDocumentUtilsTest.NS2, XmlDocumentUtilsTest.REVISION, "cont2");
        findFirstSchema = XmlDocumentUtils.findFirstSchema(ID, null);
        Assert.assertFalse(findFirstSchema.isPresent());
    }

    @Test
    public void xmlDocFindFirstSchemaContChoicTest() {
        final QName ID = QName.create(XmlDocumentUtilsTest.NS3, XmlDocumentUtilsTest.REVISION, "cont");
        final DataSchemaNode sch = Mockito.mock(ChoiceSchemaNode.class);
        final Set<ChoiceCaseNode> setCases = new HashSet<>();
        final ChoiceCaseNode choice = Mockito.mock(ChoiceCaseNode.class);
        setCases.add(choice);
        Mockito.when(((ChoiceSchemaNode) sch).getCases()).thenReturn(setCases);
        Mockito.when(sch.getQName()).thenReturn(QName.create("badNamespace", "badLocalName"));
        final Iterable<DataSchemaNode> colls = Lists.newArrayList(sch);
        final Optional<DataSchemaNode> findFirstSchema = XmlDocumentUtils.findFirstSchema(ID, colls);
        Assert.assertNotNull(findFirstSchema);
    }

    @Test
    public void codecProviderTest() {
        final XmlCodecProvider provider = XmlDocumentUtils.defaultValueCodecProvider();
        Assert.assertNotNull(provider);
        Assert.assertEquals(XmlUtils.DEFAULT_XML_CODEC_PROVIDER, provider);

        final BinaryTypeDefinition baseType = Mockito.mock(BinaryTypeDefinition.class);
        doReturn(java.util.Optional.empty()).when(baseType).getLengthConstraint();
        final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codec = provider.codecFor(baseType);
        Assert.assertNotNull(codec);
    }
}
