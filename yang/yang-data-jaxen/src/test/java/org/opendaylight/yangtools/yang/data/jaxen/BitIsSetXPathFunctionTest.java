/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.net.URI;
import java.text.ParseException;
import java.util.Set;
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

public class BitIsSetXPathFunctionTest {

    private static JaxenSchemaContextFactory jaxenSchemaContextFactory;

    private static QNameModule fooModule;
    private static QName myContainer;
    private static QName myList;
    private static QName flags;
    private static QName ordinaryLeaf;

    @BeforeClass
    public static void setup() throws ParseException {
        jaxenSchemaContextFactory = new JaxenSchemaContextFactory();

        fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        myContainer = QName.create(fooModule, "my-container");
        myList = QName.create(fooModule, "my-list");
        flags = QName.create(fooModule, "flags");
        ordinaryLeaf = QName.create(fooModule, "ordinary-leaf");
    }

    @Test
    public void testBitIsSetFunction() throws Exception {
        final Set<String> setOfBits = ImmutableSet.of("UP", "PROMISCUOUS");

        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(BitIsSetXPathFunctionTest.class,
                "/yang-xpath-functions-test/bit-is-set-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(setOfBits));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToFlagsLeafNode(setOfBits));

        final Function bitIsSetFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "bit-is-set");
        boolean bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("UP"));
        assertTrue(bitIsSetResult);
        bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("PROMISCUOUS"));
        assertTrue(bitIsSetResult);
        bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("DISABLED"));
        assertFalse(bitIsSetResult);
    }

    @Test
    public void testInvalidTypeOfCorrespondingSchemaNode() throws Exception {
        final Set<String> setOfBits = ImmutableSet.of("UP", "PROMISCUOUS");

        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(BitIsSetXPathFunctionTest.class,
                "/yang-xpath-functions-test/bit-is-set-function/foo-invalid.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(setOfBits));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToFlagsLeafNode(setOfBits));

        final Function bitIsSetFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "bit-is-set");
        boolean bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("UP"));
        assertFalse(bitIsSetResult);
    }

    @Test
    public void testInvalidNormalizedNodeValueType() throws Exception {
        final String invalidNodeValueType = "value of invalid type";

        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(BitIsSetXPathFunctionTest.class,
                "/yang-xpath-functions-test/bit-is-set-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(
                    invalidNodeValueType));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToFlagsLeafNode(invalidNodeValueType));

        final Function bitIsSetFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "bit-is-set");
        boolean bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("UP"));
        assertFalse(bitIsSetResult);
    }

    @Test
    public void shouldFailOnUnknownBitArgument() throws Exception {
        final Set<String> setOfBits = ImmutableSet.of("UP", "PROMISCUOUS");

        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(BitIsSetXPathFunctionTest.class,
                "/yang-xpath-functions-test/bit-is-set-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(setOfBits));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToFlagsLeafNode(setOfBits));

        final Function bitIsSetFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "bit-is-set");
        try {
            bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("UNKNOWN"));
            fail("Function call should have failed on unknown bit-name argument");
        } catch (final IllegalStateException ex) {
            assertTrue(ex.getMessage().startsWith("Bit UNKNOWN does not belong to bits"));
        }
    }

    @Test
    public void shouldFailOnInvalidNumberOfArguments() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function bitIsSetFunction = yangFunctionContext.getFunction(null, null, "bit-is-set");

        final Context mockedContext = mock(Context.class);

        try {
            bitIsSetFunction.call(mockedContext, ImmutableList.of("bit-a", "bit-b"));
            fail("Function call should have failed on invalid number of arguments.");
        } catch (final FunctionCallException ex) {
            assertEquals("bit-is-set() takes two arguments: node-set nodes, string bit-name", ex.getMessage());
        }
    }

    @Test
    public void shouldFailOnInvalidTypeOfArgument() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function bitIsSetFunction = yangFunctionContext.getFunction(null, null, "bit-is-set");

        final Context mockedContext = mock(Context.class);

        try {
            bitIsSetFunction.call(mockedContext, ImmutableList.of(100));
            fail("Function call should have failed on invalid type of the bit-name argument.");
        } catch (final FunctionCallException ex) {
            assertEquals("Argument bit-name of bit-is-set() function should be a String", ex.getMessage());
        }
    }

    private static ContainerNode buildMyContainerNode(final Object keyLeafValue) {
        final LeafNode<?> ordinaryLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(ordinaryLeaf))
                .withValue("test-value").build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, flags, keyLeafValue))
                        .withChild(ordinaryLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(myListNode).build();

        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToFlagsLeafNode(final Object keyLeafValue) {
        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(flags, keyLeafValue).build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(myList)
                .node(new NodeIdentifierWithPredicates(myList, keys)).node(flags);
        return path;
    }
}
