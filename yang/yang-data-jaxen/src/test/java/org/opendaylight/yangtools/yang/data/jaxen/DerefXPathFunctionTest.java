/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import org.jaxen.Function;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class DerefXPathFunctionTest {

    private static JaxenSchemaContextFactory jaxenSchemaContextFactory;

    private static QNameModule fooModule;
    private static QName myContainer;
    private static QName myInnerContainer;
    private static QName myList;
    private static QName keyLeafA;
    private static QName keyLeafB;
    private static QName iidLeaf;
    private static QName referencedLeaf;
    private static QName referencedLeafList;
    private static QName absLeafrefLeaf;
    private static QName relLeafrefLeaf;
    private static QName leafListLeafrefLeaf;
    private static QName ordinaryLeafA;
    private static QName ordinaryLeafB;

    @BeforeClass
    public static void setup() throws ParseException {
        jaxenSchemaContextFactory = new JaxenSchemaContextFactory();

        fooModule = QNameModule.create(URI.create("foo-ns"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2017-04-03"));
        myContainer = QName.create(fooModule, "my-container");
        myInnerContainer = QName.create(fooModule, "my-inner-container");
        myList = QName.create(fooModule, "my-list");
        keyLeafA = QName.create(fooModule, "key-leaf-a");
        keyLeafB = QName.create(fooModule, "key-leaf-b");
        iidLeaf = QName.create(fooModule, "iid-leaf");
        referencedLeaf = QName.create(fooModule, "referenced-leaf");
        referencedLeafList = QName.create(fooModule, "referenced-leaf-list");
        absLeafrefLeaf = QName.create(fooModule, "abs-leafref-leaf");
        relLeafrefLeaf = QName.create(fooModule, "rel-leafref-leaf");
        leafListLeafrefLeaf = QName.create(fooModule, "leaf-list-leafref-leaf");
        ordinaryLeafA = QName.create(fooModule, "ordinary-leaf-a");
        ordinaryLeafB = QName.create(fooModule, "ordinary-leaf-b");
    }

    @Test
    public void testDerefFunctionForInstanceIdentifier() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerefXPathFunctionTest.class,
                "/yang-xpath-functions-test/deref-function-iid/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);

        final LeafNode<?> referencedLeafNode = Builders.leafBuilder().withNodeIdentifier(
            new NodeIdentifier(referencedLeaf)).withValue("referenced-leaf-node-value").build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForIIdTest(
            referencedLeafNode));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(
                buildPathToIIdLeafNode());

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        final Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);
        assertSame(referencedLeafNode, derefResult);
    }

    @Test
    public void testDerefFunctionForLeafref() throws Exception {
        // tests absolute and relative leafref that references a leaf node
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerefXPathFunctionTest.class,
                "/yang-xpath-functions-test/deref-function-leafref/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);

        final LeafNode<?> referencedLeafNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(referencedLeaf)).withValue("referenced-leaf-node-value").build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForLeafrefTest(
                referencedLeafNode));

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
    public void testDerefFunctionForLeafref2() throws Exception {
        // tests leafref that references a leaf-list node
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerefXPathFunctionTest.class,
                "/yang-xpath-functions-test/deref-function-leafref/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);

        final LeafSetNode<?> referencedLeafListNode = Builders.leafSetBuilder().withNodeIdentifier(
                new NodeIdentifier(referencedLeafList))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(referencedLeafList, "referenced-node-entry-value-a"))
                        .withValue("referenced-node-entry-value-a").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(referencedLeafList, "referenced-node-entry-value-b"))
                        .withValue("referenced-node-entry-value-b").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(referencedLeafList, "referenced-node-entry-value-c"))
                        .withValue("referenced-node-entry-value-c").build())
                .build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForLeafrefTest(
                referencedLeafListNode));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", fooModule);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final YangInstanceIdentifier leafListLeafrefPath = YangInstanceIdentifier.of(myInnerContainer)
                .node(leafListLeafrefLeaf);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport
                .createContext(leafListLeafrefPath);

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);

        final LeafSetEntryNode<?> referencedLeafListNodeEntry = referencedLeafListNode.getChild(
                new NodeWithValue<>(referencedLeafList, "referenced-node-entry-value-b")).get();
        assertSame(referencedLeafListNodeEntry, derefResult);
    }

    private static ContainerNode buildMyContainerNodeForIIdTest(final LeafNode<?> referencedLeafNode) {
        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "key-value-a", keyLeafB, "key-value-b");
        final YangInstanceIdentifier iidPath = YangInstanceIdentifier.of(myContainer).node(myList)
                .node(new NodeIdentifierWithPredicates(myList, keyValues)).node(referencedLeaf);

        final LeafNode<?> iidLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(iidLeaf))
                .withValue(iidPath).build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, keyValues))
                        .withChild(iidLeafNode)
                        .withChild(referencedLeafNode).build())
                .build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer)).withChild(myListNode).build();
        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToIIdLeafNode() {
        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "key-value-a", keyLeafB, "key-value-b");
        final YangInstanceIdentifier path = YangInstanceIdentifier.of(myList)
                .node(new NodeIdentifierWithPredicates(myList, keyValues)).node(iidLeaf);
        return path;
    }

    // variant for a leafref that references a leaf
    private static ContainerNode buildMyContainerNodeForLeafrefTest(final LeafNode<?> referencedLeafNode) {
        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "value-a", keyLeafB, "value-b");

        final LeafNode<?> absLeafrefNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(absLeafrefLeaf))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> relLeafrefNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(relLeafrefLeaf))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> ordinaryLeafANode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafA)).withValue("value-a").build();
        final LeafNode<?> ordinaryLeafBNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafB)).withValue("value-b").build();

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
        return myContainerNode;
    }

    // variant for a leafref that references a leaf-list
    private static ContainerNode buildMyContainerNodeForLeafrefTest(final LeafSetNode<?> referencedLeafListNode) {
        final LeafNode<?> leafListLeafrefNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(leafListLeafrefLeaf)).withValue("referenced-node-entry-value-b").build();

        final LeafNode<?> ordinaryLeafANode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafA)).withValue("value-a").build();
        final LeafNode<?> ordinaryLeafBNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ordinaryLeafB)).withValue("value-b").build();

        final ContainerNode myInnerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myInnerContainer))
                .withChild(leafListLeafrefNode)
                .withChild(ordinaryLeafANode)
                .withChild(ordinaryLeafBNode).build();

        final Map<QName, Object> keyValues = ImmutableMap.of(keyLeafA, "value-a", keyLeafB, "value-b");

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myList, keyValues))
                        .withChild(referencedLeafListNode).build())
                .build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer))
                .withChild(myListNode)
                .withChild(myInnerContainerNode).build();
        return myContainerNode;
    }
}