/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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

class XmlStreamUtilsTest {
    @FunctionalInterface
    interface XMLStreamWriterConsumer {
        void accept(XMLStreamWriter writer) throws XMLStreamException;
    }

    private static EffectiveModelContext modelContext;
    private static Module leafRefModule;
    private static PreferredPrefixes pref;

    @BeforeAll
    static void initialize() {
        modelContext = YangParserTestUtils.parseYangResource("/leafref-test.yang");
        assertNotNull(modelContext);
        assertEquals(1, modelContext.getModules().size());
        leafRefModule = modelContext.getModules().iterator().next();
        assertNotNull(leafRefModule);
        pref = new PreferredPrefixes.Shared(modelContext);
    }

    @AfterAll
    static void cleanup() {
        leafRefModule = null;
        modelContext = null;
    }

    @Test
    void testWriteIdentityRef() throws Exception {
        final QNameModule parent = QNameModule.of("parent:uri", "2000-01-01");

        String xmlAsString = createXml(writer -> {
            writer.writeStartElement("element");
            final var facade = new StreamWriterFacade(writer, pref);
            facade.writeCharacters(XMLStreamWriterUtils.encode(facade, QName.create(parent, "identity"), parent));
            facade.flush();
            writer.writeEndElement();
        });

        assertThat(xmlAsString, containsString("element>identity"));

        xmlAsString = createXml(writer -> {
            writer.writeStartElement("elementDifferent");
            final var facade = new StreamWriterFacade(writer, pref);
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
    void testLeafRefRelativeChaining() {
        getTargetNodeForLeafRef(StringTypeDefinition.class, "cont3", "leafname3");
    }

    @Test
    void testLeafRefRelative() {
        getTargetNodeForLeafRef(StringTypeDefinition.class, "pointToStringLeaf");
    }

    @Test
    void testLeafRefAbsoluteWithSameTarget() {
        getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "absname");
    }

    /**
     * Tests relative path with double point inside path (e. g. "../../lf:interface/../lf:cont2/lf:stringleaf")
     */
    // ignored because this isn't implemented
    @Disabled
    @Test
    void testLeafRefWithDoublePointInPath() {
        getTargetNodeForLeafRef(StringTypeDefinition.class, "lf-with-double-point-inside");
    }

    @Test
    void testLeafRefRelativeAndAbsoluteWithSameTarget() {
        assertSame(getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "absname"),
            getTargetNodeForLeafRef(InstanceIdentifierTypeDefinition.class, "relname"));
    }

    private static TypeDefinition<?> getTargetNodeForLeafRef(final Class<?> clas, final String... names) {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(modelContext);
        stack.enterDataTree(QName.create(leafRefModule.getQNameModule(), "cont2"));
        for (String name : names) {
            stack.enterDataTree(QName.create(leafRefModule.getQNameModule(), name));
        }

        final EffectiveStatement<?, ?> leaf = stack.currentStatement();
        assertThat(leaf, instanceOf(LeafSchemaNode.class));
        final TypeDefinition<? extends TypeDefinition<?>> type = ((TypedDataSchemaNode) leaf).getType();
        assertThat(type, instanceOf(LeafrefTypeDefinition.class));

        final TypeDefinition<?> resolved = stack.resolveLeafref((LeafrefTypeDefinition) type);
        assertThat(resolved, instanceOf(clas));
        return resolved;
    }
}
