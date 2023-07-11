/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class XmlStreamUtilsTest {
    @FunctionalInterface
    interface XMLStreamWriterConsumer {
        void accept(XMLStreamWriter writer) throws XMLStreamException;
    }

    private static EffectiveModelContext schemaContext;
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
        final QNameModule parent = QNameModule.create(XMLNamespace.of("parent:uri"), Revision.of("2000-01-01"));

        String xmlAsString = createXml(writer -> {
            writer.writeStartElement("element");
            final StreamWriterFacade facade = new StreamWriterFacade(writer);
            facade.writeCharacters(XMLStreamWriterUtils.encode(facade, QName.create(parent, "identity"), parent));
            facade.flush();
            writer.writeEndElement();
        });

        assertTrue(xmlAsString.contains("element>identity"));

        xmlAsString = createXml(writer -> {
            writer.writeStartElement("elementDifferent");
            final StreamWriterFacade facade = new StreamWriterFacade(writer);
            facade.writeCharacters(XMLStreamWriterUtils.encode(facade, QName.create("different:namespace", "identity"),
                parent));
            facade.flush();
            writer.writeEndElement();
        });

        final Pattern prefixedIdentityPattern = Pattern.compile(".*\"different:namespace\">(.*):identity.*");
        final Matcher matcher = prefixedIdentityPattern.matcher(xmlAsString);
        assertTrue(matcher.matches(), "Xml: " + xmlAsString + " should match: " + prefixedIdentityPattern);
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
        getTargetNodeForLeafRef(StringTypeDefinition.class, "cont3", "leafname3");
    }

    @Test
    public void testLeafRefRelative() {
        getTargetNodeForLeafRef(StringTypeDefinition.class, "pointToStringLeaf");
    }

    @Test
    public void testLeafRefAbsoluteWithSameTarget() {
        getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "absname");
    }

    /**
     * Tests relative path with double point inside path (e. g. "../../lf:interface/../lf:cont2/lf:stringleaf")
     */
    // ignored because this isn't implemented
    @Ignore
    @Test
    public void testLeafRefWithDoublePointInPath() {
        getTargetNodeForLeafRef(StringTypeDefinition.class, "lf-with-double-point-inside");
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteWithSameTarget() {
        assertSame(getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "absname"),
            getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "relname"));
    }

    private static TypeDefinition<?> getTargetNodeForLeafRef(final Class<?> clas, final String... names) {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(schemaContext);
        stack.enterDataTree(QName.create(leafRefModule.getQNameModule(), "cont2"));
        for (String name : names) {
            stack.enterDataTree(QName.create(leafRefModule.getQNameModule(), name));
        }

        final EffectiveStatement<?, ?> leaf = stack.currentStatement();
        assertInstanceOf(LeafSchemaNode.class, leaf);
        final TypeDefinition<? extends TypeDefinition<?>> type = ((TypedDataSchemaNode) leaf).getType();
        assertInstanceOf(LeafrefTypeDefinition.class, type);

        final TypeDefinition<?> resolved = stack.resolveLeafref((LeafrefTypeDefinition) type);
        assertInstanceOf(clas, resolved);
        return resolved;
    }
}
