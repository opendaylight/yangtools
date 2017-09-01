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
import java.text.ParseException;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class EnumValueXPathFunctionTest {

    private static JaxenSchemaContextFactory jaxenSchemaContextFactory;

    private static QNameModule fooModule;
    private static QName myContainer;
    private static QName alarm;
    private static QName severity;
    private static QName ordinaryLeaf;

    @BeforeClass
    public static void setup() throws ParseException {
        jaxenSchemaContextFactory = new JaxenSchemaContextFactory();

        fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        myContainer = QName.create(fooModule, "my-container");
        alarm = QName.create(fooModule, "alarm");
        severity = QName.create(fooModule, "severity");
        ordinaryLeaf = QName.create(fooModule, "ordinary-leaf");
    }

    @Test
    public void testEnumValueFunction() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(EnumValueXPathFunctionTest.class,
                "/yang-xpath-functions-test/enum-value-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("major"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

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

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("major"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

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

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(100));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

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

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("unknown"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

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
        final LeafNode<?> ordinaryLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(ordinaryLeaf))
                .withValue("test-value").build();

        final MapNode alarmListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(alarm))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(alarm, severity, keyLeafValue))
                        .withChild(ordinaryLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(alarmListNode).build();
        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToSeverityLeafNode(final Object keyLeafValue) {
        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(severity, keyLeafValue).build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(alarm)
                .node(new NodeIdentifierWithPredicates(alarm, keys)).node(severity);
        return path;
    }
}