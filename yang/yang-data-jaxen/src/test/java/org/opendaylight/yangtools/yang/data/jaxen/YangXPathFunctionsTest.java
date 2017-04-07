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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.jaxen.Context;
import org.jaxen.Function;
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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YangXPathFunctionsTest {

    private static JaxenSchemaContextFactory jaxenSchemaContextFactory;

    @BeforeClass
    public static void setup() {
        jaxenSchemaContextFactory = new JaxenSchemaContextFactory();
    }

    @Test
    public void testRematchFunction() throws Exception {
        // re-match() uses regex processing from yang-parser-impl which has been thoroughly tested within
        // the Bug5410Test unit test class, so here is just a basic test
        final YangFunctionContext yangFunctionContext = YangFunctionContext.getInstance();
        final Function rematchFunction = yangFunctionContext.getFunction(null, null, "re-match");

        final Context mockedContext = mock(Context.class);

        boolean rematchResult = (boolean) rematchFunction.call(mockedContext, ImmutableList.of("abbc", "[abc]{1,4}"));
        assertTrue(rematchResult);
        rematchResult = (boolean) rematchFunction.call(mockedContext, ImmutableList.of("abbcc", "[abc]{1,4}"));
        assertFalse(rematchResult);
    }

    @Test
    public void testDerefFunctionForInstanceIdentifier() throws Exception {
        final QNameModule fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        final QName myContainer = QName.create(fooModule, "my-container");
        final QName myList = QName.create(fooModule, "my-list");
        final QName keyLeafA = QName.create(fooModule, "key-leaf-a");
        final QName keyLeafB = QName.create(fooModule, "key-leaf-b");
        final QName iidLeaf = QName.create(fooModule, "iid-leaf");
        final QName referencedLeaf = QName.create(fooModule, "referenced-leaf");

        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "key-value-a", keyLeafB, "key-value-b");
        final YangInstanceIdentifier iidPath = YangInstanceIdentifier.of(myContainer).node(myList)
                .node(new NodeIdentifierWithPredicates(myList, keyValues)).node(referencedLeaf);

        final LeafNode<?> iidLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(iidLeaf))
                .withValue(iidPath).build();
        final LeafNode<?> referencedLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(referencedLeaf))
                .withValue("referenced-leaf-node-value").build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, keyValues))
                        .withChild(iidLeafNode)
                        .withChild(referencedLeafNode).build())
                .build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(myListNode).build();

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource(
                "/yang-xpath-functions-test/deref-function-iid/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(myContainerNode);

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(myList)
                .node(new NodeIdentifierWithPredicates(myList, keyValues)).node(iidLeaf);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(path);

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        final Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);
        assertSame(referencedLeafNode, derefResult);
    }

    @Test
    public void testDerefFunctionForLeafref() throws Exception {
        final QNameModule fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        final QName myContainer = QName.create(fooModule, "my-container");
        final QName myInnerContainer = QName.create(fooModule, "my-inner-container");
        final QName myList = QName.create(fooModule, "my-list");
        final QName keyLeafA = QName.create(fooModule, "key-leaf-a");
        final QName keyLeafB = QName.create(fooModule, "key-leaf-b");
        final QName absLeafrefLeaf = QName.create(fooModule, "abs-leafref-leaf");
        final QName relLeafrefLeaf = QName.create(fooModule, "rel-leafref-leaf");
        final QName ordinaryLeafA = QName.create(fooModule, "ordinary-leaf-a");
        final QName ordinaryLeafB = QName.create(fooModule, "ordinary-leaf-b");
        final QName referencedLeaf = QName.create(fooModule, "referenced-leaf");

        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "value-a", keyLeafB, "value-b");

        final LeafNode<?> absLeafrefNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(absLeafrefLeaf))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> relLeafrefNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(relLeafrefLeaf))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> ordinaryLeafANode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafA)).withValue("value-a").build();
        final LeafNode<?> ordinaryLeafBNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafB)).withValue("value-b").build();

        final LeafNode<?> referencedLeafNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(referencedLeaf)).withValue("referenced-leaf-node-value").build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, keyValues))
                        .withChild(referencedLeafNode).build())
                .build();

        final ContainerNode myInnerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myInnerContainer))
                .withChild(absLeafrefNode)
                .withChild(relLeafrefNode)
                .withChild(ordinaryLeafANode)
                .withChild(ordinaryLeafBNode).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer))
                .withChild(myListNode)
                .withChild(myInnerContainerNode).build();

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource(
                "/yang-xpath-functions-test/deref-function-leafref/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(myContainerNode);

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final YangInstanceIdentifier absLeafrefPath = YangInstanceIdentifier.of(myInnerContainer).node(absLeafrefLeaf);
        NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(absLeafrefPath);

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);
        assertSame(referencedLeafNode, derefResult);

        final YangInstanceIdentifier relLeafrefPath = YangInstanceIdentifier.of(myInnerContainer).node(relLeafrefLeaf);
        normalizedNodeContext = normalizedNodeContextSupport.createContext(relLeafrefPath);

        derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);
        assertSame(referencedLeafNode, derefResult);
    }

    @Test
    public void testDerivedFromFunction() throws Exception {
        // also includes test for derived-from-or-self function
        final QNameModule barModule = QNameModule.create(URI.create("bar-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        final QName myContainer = QName.create(barModule, "my-container");
        final QName myList = QName.create(barModule, "my-list");
        final QName keyLeaf = QName.create(barModule, "key-leaf");
        final QName idrefLeaf = QName.create(barModule, "idref-leaf");
        final QName idC2Identity = QName.create(barModule, "id-c2");

        final LeafNode<?> idrefLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(idrefLeaf))
                .withValue(idC2Identity).build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, keyLeaf, "key-value"))
                        .withChild(idrefLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(myListNode).build();

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSources(
                "/yang-xpath-functions-test/derived-from-function");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(myContainerNode);

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("bar-prefix", barModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(keyLeaf, "key-value").build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(myList)
                .node(new NodeIdentifierWithPredicates(myList, keys)).node(idrefLeaf);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(path);

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
    public void testEnumValueFunction() throws Exception {
        final QNameModule fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        final QName myContainer = QName.create(fooModule, "my-container");
        final QName alarm = QName.create(fooModule, "alarm");
        final QName severity = QName.create(fooModule, "severity");
        final QName ordinaryLeaf = QName.create(fooModule, "ordinary-leaf");

        final LeafNode<?> ordinaryLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(ordinaryLeaf))
                .withValue("test-value").build();

        final MapNode alarmListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(alarm))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(alarm, severity, "major"))
                        .withChild(ordinaryLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(alarmListNode).build();

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource(
                "/yang-xpath-functions-test/enum-value-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(myContainerNode);

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(severity, "major").build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(alarm)
                .node(new NodeIdentifierWithPredicates(alarm, keys)).node(severity);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(path);

        final Function enumValueFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "enum-value");
        final int enumValueResult = (int) enumValueFunction.call(normalizedNodeContext, ImmutableList.of());
        assertEquals(5, enumValueResult);
    }

    @Test
    public void testBitIsSetFunction() throws Exception {
        final QNameModule fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        final QName myContainer = QName.create(fooModule, "my-container");
        final QName myList = QName.create(fooModule, "my-list");
        final QName flags = QName.create(fooModule, "flags");
        final QName ordinaryLeaf = QName.create(fooModule, "ordinary-leaf");

        final LeafNode<?> ordinaryLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(ordinaryLeaf))
                .withValue("test-value").build();

        final Set<String> setOfBits = ImmutableSet.of("UP", "PROMISCUOUS");

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, flags, setOfBits))
                        .withChild(ordinaryLeafNode).build()).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(myListNode).build();

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource(
                "/yang-xpath-functions-test/bit-is-set-function/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);
        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(myContainerNode);

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        final ImmutableMap<QName, Object> keys = builder.put(flags, setOfBits).build();

        final YangInstanceIdentifier path = YangInstanceIdentifier.of(myList)
                .node(new NodeIdentifierWithPredicates(myList, keys)).node(flags);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(path);

        final Function bitIsSetFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "bit-is-set");
        boolean bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("UP"));
        assertTrue(bitIsSetResult);
        bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("PROMISCUOUS"));
        assertTrue(bitIsSetResult);
        bitIsSetResult = (boolean) bitIsSetFunction.call(normalizedNodeContext, ImmutableList.of("DISABLED"));
        assertFalse(bitIsSetResult);
    }

    private static boolean getDerivedFromResult(final Function derivedFromFunction, final NormalizedNodeContext nnCtx,
            final String identityArg) throws Exception {
        return (boolean) derivedFromFunction.call(nnCtx, ImmutableList.of(identityArg));
    }
}
