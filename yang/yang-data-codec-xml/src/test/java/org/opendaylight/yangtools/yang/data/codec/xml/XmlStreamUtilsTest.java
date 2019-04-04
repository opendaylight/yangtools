/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class XmlStreamUtilsTest {
    @FunctionalInterface
    interface XMLStreamWriterConsumer {
        void accept(XMLStreamWriter writer) throws XMLStreamException;
    }

    private static SchemaContext schemaContext;
    private static Module leafRefModule;

    @BeforeClass
    public static void initialize() {
        schemaContext = YangParserTestUtils.parseYangResource("/leafref-test.yang");
        assertNotNull(schemaContext);
        assertEquals(1, schemaContext.getModules().size());
        leafRefModule = schemaContext.getModules().iterator().next();
        assertNotNull(leafRefModule);
    }

    @AfterClass
    public static void cleanup() {
        leafRefModule = null;
        schemaContext = null;
    }

    @Test
    public void testWriteIdentityRef() throws Exception {
        final QNameModule parent = QNameModule.create(URI.create("parent:uri"), Revision.of("2000-01-01"));

        String xmlAsString = createXml(writer -> {
            writer.writeStartElement("element");
            final StreamWriterFacade facade = new StreamWriterFacade(writer);
            facade.writeCharacters(XMLStreamWriterUtils.encode(facade, null, QName.create(parent, "identity"), parent));
            facade.flush();
            writer.writeEndElement();
        });

        assertThat(xmlAsString, containsString("element>identity"));

        xmlAsString = createXml(writer -> {
            writer.writeStartElement("elementDifferent");
            final StreamWriterFacade facade = new StreamWriterFacade(writer);
            facade.writeCharacters(XMLStreamWriterUtils.encode(facade, null, QName.create("different:namespace",
                "identity"), parent));
            facade.flush();
            writer.writeEndElement();
        });

        final Pattern prefixedIdentityPattern = Pattern.compile(".*\"different:namespace\">(.*):identity.*");
        final Matcher matcher = prefixedIdentityPattern.matcher(xmlAsString);
        assertTrue("Xml: " + xmlAsString + " should match: " + prefixedIdentityPattern, matcher.matches());
    }

    private static String createXml(final XMLStreamWriterConsumer consumer) throws XMLStreamException, IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLStreamWriter writer = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(out);

        consumer.accept(writer);

        writer.close();
        out.close();

        return new String(out.toByteArray()).replaceAll("\\s*", "");
    }

    /**
     * One leafref reference to other leafref via relative references.
     */
    @Test
    public void testLeafRefRelativeChaining() {
        getTargetNodeForLeafRef("leafname3", StringTypeDefinition.class);
    }

    @Test
    public void testLeafRefRelative() {
        getTargetNodeForLeafRef("pointToStringLeaf", StringTypeDefinition.class);
    }

    @Test
    public void testLeafRefAbsoluteWithSameTarget() {
        getTargetNodeForLeafRef("absname", InstanceIdentifierTypeDefinition.class);
    }

    /**
     * Tests relative path with double point inside path (e. g. "../../lf:interface/../lf:cont2/lf:stringleaf")
     */
    // ignored because this isn't implemented
    @Ignore
    @Test
    public void testLeafRefWithDoublePointInPath() {
        getTargetNodeForLeafRef("lf-with-double-point-inside", StringTypeDefinition.class);
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteWithSameTarget() {
        final TypeDefinition<?> targetNodeForAbsname = getTargetNodeForLeafRef("absname",
            InstanceIdentifierTypeDefinition.class);
        final TypeDefinition<?> targetNodeForRelname = getTargetNodeForLeafRef("relname",
            InstanceIdentifierTypeDefinition.class);
        assertEquals(targetNodeForAbsname, targetNodeForRelname);
    }

    private TypeDefinition<?> getTargetNodeForLeafRef(final String nodeName, final Class<?> clas) {
        final LeafSchemaNode schemaNode = findSchemaNodeWithLeafrefType(leafRefModule, nodeName);
        assertNotNull(schemaNode);
        final LeafrefTypeDefinition leafrefTypedef = findLeafrefType(schemaNode);
        assertNotNull(leafrefTypedef);
        final TypeDefinition<?> targetBaseType = SchemaContextUtil.getBaseTypeForLeafRef(leafrefTypedef, schemaContext,
                schemaNode);
        assertTrue("Wrong class found.", clas.isInstance(targetBaseType));
        return targetBaseType;
    }

    private static Map<String, String> mapPrefixed(final Iterable<Map.Entry<URI, String>> prefixes) {
        final Map<String, String> mappedPrefixes = new HashMap<>();
        for (final Map.Entry<URI, String> prefix : prefixes) {
            mappedPrefixes.put(prefix.getKey().toString(), prefix.getValue());
        }
        return mappedPrefixes;
    }

    private static QName getAttrQName(final String namespace, final String revision, final String localName,
            final Optional<String> prefix) {
        if (prefix.isPresent()) {
            final QName moduleQName = QName.create(namespace, revision, "module");
            final QNameModule module = QNameModule.create(moduleQName.getNamespace(), moduleQName.getRevision());
            return QName.create(module, localName);
        }
        return QName.create(namespace, revision, localName);
    }

    private LeafSchemaNode findSchemaNodeWithLeafrefType(final DataNodeContainer module, final String nodeName) {
        for (final DataSchemaNode childNode : module.getChildNodes()) {
            if (childNode instanceof DataNodeContainer) {
                LeafSchemaNode leafrefFromRecursion = findSchemaNodeWithLeafrefType((DataNodeContainer) childNode,
                        nodeName);
                if (leafrefFromRecursion != null) {
                    return leafrefFromRecursion;
                }
            } else if (childNode.getQName().getLocalName().equals(nodeName) && childNode instanceof LeafSchemaNode) {
                final TypeDefinition<?> leafSchemaNodeType = ((LeafSchemaNode) childNode).getType();
                if (leafSchemaNodeType instanceof LeafrefTypeDefinition) {
                    return (LeafSchemaNode) childNode;
                }
            }
        }
        return null;
    }

    private static LeafrefTypeDefinition findLeafrefType(final LeafSchemaNode schemaNode) {
        final TypeDefinition<?> type = schemaNode.getType();
        if (type instanceof LeafrefTypeDefinition) {
            return (LeafrefTypeDefinition) type;
        }
        return null;
    }
}
