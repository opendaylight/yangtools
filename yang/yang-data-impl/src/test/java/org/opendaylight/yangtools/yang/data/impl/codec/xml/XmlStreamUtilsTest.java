/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.w3c.dom.Document;

public class XmlStreamUtilsTest {

    public static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    @Test
    public void testWriteAttribute() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLStreamWriter writer =  XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
        writer.writeStartElement("element");

        QName name = getAttrQName("namespace", "2012-12-12", "attr", Optional.of("prefix"));
        final Map.Entry<QName, String> attributeEntry = new AbstractMap.SimpleEntry<>(name, "value");

        name = getAttrQName("namespace2", "2012-12-12", "attr", Optional.<String>absent());
        final Map.Entry<QName, String> attributeEntryNoPrefix = new AbstractMap.SimpleEntry<>(name, "value");

        final RandomPrefix randomPrefix = new RandomPrefix();
        XmlStreamUtils.writeAttribute(writer, attributeEntry, randomPrefix);
        XmlStreamUtils.writeAttribute(writer, attributeEntryNoPrefix, randomPrefix);

        writer.writeEndElement();
        writer.close();
        out.close();

        final String xmlAsString = new String(out.toByteArray());

        final Map<String, String> mappedPrefixes = mapPrefixed(randomPrefix.getPrefixes());
        assertEquals(2, mappedPrefixes.size());
        final String randomPrefixValue = mappedPrefixes.get("namespace2");

        final String expectedXmlAsString = "<element xmlns:a=\"namespace\" a:attr=\"value\" xmlns:" + randomPrefixValue + "=\"namespace2\" " + randomPrefixValue + ":attr=\"value\"></element>";

        XMLUnit.setIgnoreAttributeOrder(true);
        final Document control = XMLUnit.buildControlDocument(expectedXmlAsString);
        final Document test = XMLUnit.buildTestDocument(xmlAsString);
        final Diff diff = XMLUnit.compareXML(control, test);

        final boolean identical = diff.identical();
        assertTrue("Xml differs: " + diff.toString(), identical);
    }

    @Ignore
    @Test
    public void testLeafRef() throws URISyntaxException, XMLStreamException, FactoryConfigurationError, IOException {
        String returned = Helper.getDeserializedValueFromLeafref();
        String expected = "test";

        assertEquals(expected, returned);
    }

    static class Helper {
        public static String getDeserializedValueFromLeafref() throws URISyntaxException, XMLStreamException, FactoryConfigurationError, IOException {
            YangParserImpl yangParser = new YangParserImpl();
            File file = new File(XmlStreamUtils.class.getResource("/leafref-test.yang").toURI());
            SchemaContext schemaContext = yangParser.parseFiles(Arrays.asList(file));
            Module module = schemaContext.getModules().iterator().next();

            LeafrefTypeDefinition leafrefTypedef = findLeafrefType(module);

            XmlStreamUtils xmlStremUtils = XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER, schemaContext);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(baos);

            SchemaNode schemaNode = findLeafrefType2(module);

            SchemaContextUtil.getBaseTypeForLeafRef(leafrefTypedef, schemaContext, schemaNode);
            xmlStremUtils.writeValue(xmlStreamWriter, leafrefTypedef, "test");
            baos.close();
            return baos.toString();
        }

        private static LeafrefTypeDefinition findLeafrefType(final Module module) {
            for (DataSchemaNode schemaNode: module.getChildNodes()) {

                if (schemaNode instanceof ContainerSchemaNode) {
                    for (DataSchemaNode childNode : ((ContainerSchemaNode)schemaNode).getChildNodes()) {
                        if (childNode instanceof LeafSchemaNode) {
                            LeafSchemaNode leafSchemaNode = (LeafSchemaNode)childNode;

                            TypeDefinition<?> leafSchemaNodeType = leafSchemaNode.getType();

                            if (leafSchemaNodeType instanceof LeafrefTypeDefinition) {
                                LeafrefTypeDefinition leafreftTypedef = (LeafrefTypeDefinition)leafSchemaNodeType;
                                return leafreftTypedef;
                            }
                        }
                    }
                }
            }
            return null;
        }

        private static DataNodeContainer childNode(ContainerSchemaNode schemaNode) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static SchemaNode findLeafrefType2(final Module module) {
        for (DataSchemaNode schemaNode: module.getChildNodes()) {

            if (schemaNode instanceof ContainerSchemaNode) {
                for (DataSchemaNode childNode : ((ContainerSchemaNode)schemaNode).getChildNodes()) {
                    if (childNode instanceof LeafSchemaNode) {
                        LeafSchemaNode leafSchemaNode = (LeafSchemaNode)childNode;

                        TypeDefinition<?> leafSchemaNodeType = leafSchemaNode.getType();

                        if (leafSchemaNodeType instanceof LeafrefTypeDefinition) {
                            return leafSchemaNode;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void testEmptyNodeWithAttribute() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLStreamWriter writer =  XML_OUTPUT_FACTORY.createXMLStreamWriter(out);

        final Map<QName, String> attrs = Maps.newHashMap();
        attrs.put(QName.create("namespaceAttr", "2012-12-12", "attr1"), "value");
        final QName qName = QName.create("urn:opendaylight:controller:rpc:test", "2014-07-28", "cont");
        final ImmutableCompositeNode dataAttributes = ImmutableCompositeNode.create(qName, attrs, Collections.<Node<?>>emptyList());
        XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER).writeDocument(writer, dataAttributes);

        writer.close();
        out.close();

        final String xmlAsString = new String(out.toByteArray());

        // TODO why resulting xml does not have namespace definition ? If sending xml by e.g. netconf the namespace is there but not here in test
        final String expectedXmlAsString = "<cont xmlns:a=\"namespaceAttr\" a:attr1=\"value\"></cont>";

        XMLUnit.setIgnoreAttributeOrder(true);
        final Document control = XMLUnit.buildControlDocument(expectedXmlAsString);
        final Document test = XMLUnit.buildTestDocument(xmlAsString);
        final Diff diff = XMLUnit.compareXML(control, test);

        final boolean identical = diff.identical();
        assertTrue("Xml differs: " + diff.toString(), identical);
    }

    private Map<String, String> mapPrefixed(final Iterable<Map.Entry<URI, String>> prefixes) {
        final Map<String, String> mappedPrefixes = Maps.newHashMap();
        for (final Map.Entry<URI, String> prefix : prefixes) {
            mappedPrefixes.put(prefix.getKey().toString(), prefix.getValue());
        }
        return mappedPrefixes;
    }

    private QName getAttrQName(final String namespace, final String revision, final String localName, final Optional<String> prefix) {

        if(prefix.isPresent()) {
            final QName moduleQName = QName.create(namespace, revision, "module");
            final QNameModule module = QNameModule.create(moduleQName.getNamespace(), moduleQName.getRevision());
            return QName.create(module, prefix.get(), localName);
        } else {
            return QName.create(namespace, revision, localName);
        }
    }
}
