/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.net.URI;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class EnumValueXPathFunctionTest {

    private static final JaxenSchemaContextFactory SCHEMA_CONTEXT_FACTORY = new JaxenSchemaContextFactory();
    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo-ns"), Revision.of("2017-04-03"));
    private static final QName MY_CONTAINER = QName.create(FOO_MODULE, "my-container");
    private static final QName ALARM = QName.create(FOO_MODULE, "alarm");
    private static final QName SEVERITY = QName.create(FOO_MODULE, "severity");
    private static final QName ORDINARY_LEAF = QName.create(FOO_MODULE, "ordinary-leaf");

    @Test
    public void testEnumValueFunction() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(EnumValueXPathFunctionTest.class,
                "/yang-xpath-functions-test/enum-value-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("major"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToSeverityLeafNode("major"));

        final Function enumValueFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "enum-value");
        final int enumValueResult = (int) enumValueFunction.call(normalizedNodeContext, ImmutableList.of());
        assertEquals(5, enumValueResult);
    }

    @Test
    public void testInvalidTypeOfCorrespondingSchemaNode() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(EnumValueXPathFunctionTest.class,
                "/yang-xpath-functions-test/enum-value-function/foo-invalid.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("major"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToSeverityLeafNode("major"));

        final Function enumValueFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "enum-value");
        final Double enumValueResult = (Double) enumValueFunction.call(normalizedNodeContext, ImmutableList.of());
        assertEquals(Double.NaN, enumValueResult, 0.001);
    }

    @Test
    public void testInvalidNormalizedNodeValueType() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(EnumValueXPathFunctionTest.class,
                "/yang-xpath-functions-test/enum-value-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(100));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToSeverityLeafNode(100));

        final Function enumValueFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "enum-value");
        final Double enumValueResult = (Double) enumValueFunction.call(normalizedNodeContext, ImmutableList.of());
        assertEquals(Double.NaN, enumValueResult, 0.001);
    }

    @Test
    public void shouldFailOnUnknownEnumNodeValue() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(EnumValueXPathFunctionTest.class,
                "/yang-xpath-functions-test/enum-value-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("unknown"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToSeverityLeafNode("unknown"));

        final Function enumValueFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "enum-value");
        try {
            enumValueFunction.call(normalizedNodeContext, ImmutableList.of());
            fail("Function call should have failed on unknown enum node value");
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().startsWith("Enum unknown does not belong to enumeration"));
        }
    }

    @Test
    public void shouldFailOnInvalidNumberOfArguments() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function enumValueFunction = yangFunctionContext.getFunction(null, null, "enum-value");

        final Context mockedContext = mock(Context.class);

        try {
            enumValueFunction.call(mockedContext, ImmutableList.of("should not be here"));
            fail("Function call should have failed on invalid number of arguments.");
        } catch (final FunctionCallException ex) {
            assertEquals("enum-value() takes one argument: node-set nodes.", ex.getMessage());
        }
    }

    private static ContainerNode buildMyContainerNode(final Object keyLeafValue) {
        final LeafNode<?> ordinaryLeafNode = Builders.leafBuilder()
                .withNodeIdentifier(new NodeIdentifier(ORDINARY_LEAF)).withValue("test-value").build();

        final MapNode alarmListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(ALARM))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(ALARM, SEVERITY, keyLeafValue))
                        .withChild(ordinaryLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER)).withChild(alarmListNode).build();
        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToSeverityLeafNode(final Object keyLeafValue) {
        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(SEVERITY, keyLeafValue).build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(ALARM)
                .node(new NodeIdentifierWithPredicates(ALARM, keys)).node(SEVERITY);
        return path;
    }
}