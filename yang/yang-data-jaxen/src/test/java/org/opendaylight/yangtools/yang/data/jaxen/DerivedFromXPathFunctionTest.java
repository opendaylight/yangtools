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

public class DerivedFromXPathFunctionTest {

    private static final JaxenSchemaContextFactory SCHEMA_CONTEXT_FACTORY = new JaxenSchemaContextFactory();
    private static final QNameModule BAR_MODULE = QNameModule.create(URI.create("bar-ns"), Revision.of("2017-04-03"));
    private static final QName MY_CONTAINER = QName.create(BAR_MODULE, "my-container");
    private static final QName MY_LIST = QName.create(BAR_MODULE, "my-list");
    private static final QName KEY_LEAF = QName.create(BAR_MODULE, "key-leaf");
    private static final QName IDREF_LEAF = QName.create(BAR_MODULE, "idref-leaf");
    private static final QName ID_C2_IDENTITY = QName.create(BAR_MODULE, "id-c2");

    @Test
    public void testDerivedFromFunction() throws Exception {
        // also includes test for derived-from-or-self function
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/foo.yang",
                "/yang-xpath-functions-test/derived-from-function/bar.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(ID_C2_IDENTITY));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        assertTrue(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a3"));
        assertTrue(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a4"));
        assertTrue(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-b2"));
        assertTrue(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "bar-prefix:id-b3"));
        assertTrue(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "id-b4"));

        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a1"));
        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a2"));
        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-b1"));
        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-c1"));
        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "bar-prefix:id-c2"));

        final Function derivedFromOrSelfFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from-or-self");
        assertTrue(getDerivedFromResult(derivedFromOrSelfFunction, normalizedNodeContext, "bar-prefix:id-c2"));
    }

    @Test
    public void testInvalidTypeOfCorrespondingSchemaNode() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/bar-invalid.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(ID_C2_IDENTITY));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "some-identity"));
    }

    @Test
    public void testInvalidNormalizedNodeValueType() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/foo.yang",
                "/yang-xpath-functions-test/derived-from-function/bar.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode("should be QName"));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        assertFalse(getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a3"));
    }

    @Test
    public void shouldFailOnUnknownPrefixOfIdentity() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/foo.yang",
                "/yang-xpath-functions-test/derived-from-function/bar.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(ID_C2_IDENTITY));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        try {
            getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "unknown-prefix:id-a3");
            fail("Function call should have failed on unresolved prefix of the identity argument.");
        } catch (IllegalArgumentException ex) {
            assertEquals("Cannot resolve prefix 'unknown-prefix' from identity 'unknown-prefix:id-a3'.",
                ex.getMessage());
        }
    }

    @Test
    public void shouldFailOnMalformedIdentityArgument() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/foo.yang",
                "/yang-xpath-functions-test/derived-from-function/bar.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(ID_C2_IDENTITY));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        try {
            getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo:bar:id-a3");
            fail("Function call should have failed on malformed identity argument.");
        } catch (IllegalArgumentException ex) {
            assertEquals("Malformed identity argument: foo:bar:id-a3.", ex.getMessage());
        }
    }

    @Test
    public void shouldFailOnUnknownIdentityArgument() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerivedFromXPathFunctionTest.class,
                "/yang-xpath-functions-test/derived-from-function/foo.yang",
                "/yang-xpath-functions-test/derived-from-function/bar.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = SCHEMA_CONTEXT_FACTORY.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNode(ID_C2_IDENTITY));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", BAR_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIdrefLeafNode());

        final Function derivedFromFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "derived-from");

        try {
            getDerivedFromResult(derivedFromFunction, normalizedNodeContext, "foo-prefix:id-a333");
            fail("Function call should have failed on unknown identity argument.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().startsWith(
                    "Identity (foo-ns?revision=2017-04-03)id-a333 does not have a corresponding identity schema "
                    + "node in the module"));
        }
    }

    @Test
    public void shouldFailOnInvalidNumberOfArguments() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function derivedFromFunction = yangFunctionContext.getFunction(null, null, "derived-from");

        final Context mockedContext = mock(Context.class);

        try {
            derivedFromFunction.call(mockedContext, ImmutableList.of("some-identity", "should not be here"));
            fail("Function call should have failed on invalid number of arguments.");
        } catch (final FunctionCallException ex) {
            assertEquals("derived-from() takes two arguments: node-set nodes, string identity", ex.getMessage());
        }
    }

    @Test
    public void shouldFailOnInvalidTypeOfArgument() throws Exception {
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function bitIsSetFunction = yangFunctionContext.getFunction(null, null, "derived-from");

        final Context mockedContext = mock(Context.class);

        try {
            bitIsSetFunction.call(mockedContext, ImmutableList.of(100));
            fail("Function call should have failed on invalid type of the identity argument.");
        } catch (final FunctionCallException ex) {
            assertEquals("Argument 'identity' of derived-from() function should be a String.", ex.getMessage());
        }
    }

    private static boolean getDerivedFromResult(final Function derivedFromFunction, final NormalizedNodeContext nnCtx,
            final String identityArg) throws Exception {
        return (boolean) derivedFromFunction.call(nnCtx, ImmutableList.of(identityArg));
    }

    private static ContainerNode buildMyContainerNode(final Object idrefLeafValue) {
        final LeafNode<?> idrefLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(IDREF_LEAF))
                .withValue(idrefLeafValue).build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_LIST, KEY_LEAF, "key-value"))
                        .withChild(idrefLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER)).withChild(myListNode).build();
        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToIdrefLeafNode() {
        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(KEY_LEAF, "key-value").build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(MY_LIST)
                .node(new NodeIdentifierWithPredicates(MY_LIST, keys)).node(IDREF_LEAF);
        return path;
    }
}
