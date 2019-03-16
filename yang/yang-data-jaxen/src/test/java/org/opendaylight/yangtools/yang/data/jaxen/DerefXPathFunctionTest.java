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
import java.util.Map;
import org.jaxen.Function;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
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
import org.opendaylight.yangtools.yang.data.api.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class DerefXPathFunctionTest {

    private static JaxenSchemaContextFactory jaxenSchemaContextFactory = new JaxenSchemaContextFactory();

    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo-ns"), Revision.of("2017-04-03"));
    private static final QName MY_CONTAINER = QName.create(FOO_MODULE, "my-container");
    private static final QName MY_INNER_CONTAINER = QName.create(FOO_MODULE, "my-inner-container");
    private static final QName MY_LIST = QName.create(FOO_MODULE, "my-list");
    private static final QName KEY_LEAF_A = QName.create(FOO_MODULE, "key-leaf-a");
    private static final QName KEY_LEAF_B = QName.create(FOO_MODULE, "key-leaf-b");
    private static final QName IID_LEAF = QName.create(FOO_MODULE, "iid-leaf");
    private static final QName REFERENCED_LEAF = QName.create(FOO_MODULE, "referenced-leaf");
    private static final QName REFERENCED_LEAFLIST = QName.create(FOO_MODULE, "referenced-leaf-list");
    private static final QName ABS_LEAFREF_LEAF = QName.create(FOO_MODULE, "abs-leafref-leaf");
    private static final QName REL_LEAFREF_LEAF = QName.create(FOO_MODULE, "rel-leafref-leaf");
    private static final QName LEAFLIST_LEAFREF_LEAF = QName.create(FOO_MODULE, "leaf-list-leafref-leaf");
    private static final QName ORDINARY_LEAF_A = QName.create(FOO_MODULE, "ordinary-leaf-a");
    private static final QName ORDINARY_LEAF_B = QName.create(FOO_MODULE, "ordinary-leaf-b");

    @Test
    public void testDerefFunctionForInstanceIdentifier() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResources(DerefXPathFunctionTest.class,
                "/yang-xpath-functions-test/deref-function-iid/foo.yang");
        assertNotNull(schemaContext);

        final XPathSchemaContext jaxenSchemaContext = jaxenSchemaContextFactory.createContext(schemaContext);

        final LeafNode<?> referencedLeafNode = Builders.leafBuilder().withNodeIdentifier(
            new NodeIdentifier(REFERENCED_LEAF)).withValue("referenced-leaf-node-value").build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForIIdTest(
            referencedLeafNode));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

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
                new NodeIdentifier(REFERENCED_LEAF)).withValue("referenced-leaf-node-value").build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForLeafrefTest(
                referencedLeafNode));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final YangInstanceIdentifier absLeafrefPath = YangInstanceIdentifier.of(MY_INNER_CONTAINER)
                .node(ABS_LEAFREF_LEAF);
        NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport.createContext(absLeafrefPath);

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);
        assertSame(referencedLeafNode, derefResult);

        final YangInstanceIdentifier relLeafrefPath = YangInstanceIdentifier.of(MY_INNER_CONTAINER)
                .node(REL_LEAFREF_LEAF);
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
                new NodeIdentifier(REFERENCED_LEAFLIST))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(REFERENCED_LEAFLIST, "referenced-node-entry-value-a"))
                        .withValue("referenced-node-entry-value-a").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(REFERENCED_LEAFLIST, "referenced-node-entry-value-b"))
                        .withValue("referenced-node-entry-value-b").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(REFERENCED_LEAFLIST, "referenced-node-entry-value-c"))
                        .withValue("referenced-node-entry-value-c").build())
                .build();

        final XPathDocument jaxenDocument = jaxenSchemaContext.createDocument(buildMyContainerNodeForLeafrefTest(
                referencedLeafListNode));

        final BiMap<String, QNameModule> converterBiMap = HashBiMap.create();
        converterBiMap.put("foo-prefix", FOO_MODULE);

        final NormalizedNodeContextSupport normalizedNodeContextSupport = NormalizedNodeContextSupport.create(
                (JaxenDocument) jaxenDocument, Maps.asConverter(converterBiMap));

        final YangInstanceIdentifier leafListLeafrefPath = YangInstanceIdentifier.of(MY_INNER_CONTAINER)
                .node(LEAFLIST_LEAFREF_LEAF);
        final NormalizedNodeContext normalizedNodeContext = normalizedNodeContextSupport
                .createContext(leafListLeafrefPath);

        final Function derefFunction = normalizedNodeContextSupport.getFunctionContext()
                .getFunction(null, null, "deref");
        Object derefResult = derefFunction.call(normalizedNodeContext, ImmutableList.of());
        assertNotNull(derefResult);
        assertTrue(derefResult instanceof NormalizedNode<?, ?>);

        final LeafSetEntryNode<?> referencedLeafListNodeEntry = referencedLeafListNode.getChild(
                new NodeWithValue<>(REFERENCED_LEAFLIST, "referenced-node-entry-value-b")).get();
        assertSame(referencedLeafListNodeEntry, derefResult);
    }

    private static ContainerNode buildMyContainerNodeForIIdTest(final LeafNode<?> referencedLeafNode) {
        final Map<QName, Object> keyValues = ImmutableMap.of(KEY_LEAF_A, "key-value-a", KEY_LEAF_B, "key-value-b");
        final YangInstanceIdentifier iidPath = YangInstanceIdentifier.of(MY_CONTAINER).node(MY_LIST)
                .node(new NodeIdentifierWithPredicates(MY_LIST, keyValues)).node(REFERENCED_LEAF);

        final LeafNode<?> iidLeafNode = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(IID_LEAF))
                .withValue(iidPath).build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_LIST, keyValues))
                        .withChild(iidLeafNode)
                        .withChild(referencedLeafNode).build())
                .build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER)).withChild(myListNode).build();
        return myContainerNode;
    }

    private static YangInstanceIdentifier buildPathToIIdLeafNode() {
        final Map<QName, Object> keyValues = ImmutableMap.of(KEY_LEAF_A, "key-value-a", KEY_LEAF_B, "key-value-b");
        final YangInstanceIdentifier path = YangInstanceIdentifier.of(MY_LIST)
                .node(new NodeIdentifierWithPredicates(MY_LIST, keyValues)).node(IID_LEAF);
        return path;
    }

    // variant for a leafref that references a leaf
    private static ContainerNode buildMyContainerNodeForLeafrefTest(final LeafNode<?> referencedLeafNode) {
        final Map<QName, Object> keyValues = ImmutableMap.of(KEY_LEAF_A, "value-a", KEY_LEAF_B, "value-b");

        final LeafNode<?> absLeafrefNode = Builders.leafBuilder()
                .withNodeIdentifier(new NodeIdentifier(ABS_LEAFREF_LEAF))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> relLeafrefNode = Builders.leafBuilder()
                .withNodeIdentifier(new NodeIdentifier(REL_LEAFREF_LEAF))
                .withValue("referenced-leaf-node-value").build();
        final LeafNode<?> ordinaryLeafANode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ORDINARY_LEAF_A)).withValue("value-a").build();
        final LeafNode<?> ordinaryLeafBNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ORDINARY_LEAF_B)).withValue("value-b").build();

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_LIST, keyValues))
                        .withChild(referencedLeafNode).build())
                .build();

        final ContainerNode myInnerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_INNER_CONTAINER))
                .withChild(absLeafrefNode)
                .withChild(relLeafrefNode)
                .withChild(ordinaryLeafANode)
                .withChild(ordinaryLeafBNode).build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER))
                .withChild(myListNode)
                .withChild(myInnerContainerNode).build();
        return myContainerNode;
    }

    // variant for a leafref that references a leaf-list
    private static ContainerNode buildMyContainerNodeForLeafrefTest(final LeafSetNode<?> referencedLeafListNode) {
        final LeafNode<?> leafListLeafrefNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(LEAFLIST_LEAFREF_LEAF)).withValue("referenced-node-entry-value-b").build();

        final LeafNode<?> ordinaryLeafANode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ORDINARY_LEAF_A)).withValue("value-a").build();
        final LeafNode<?> ordinaryLeafBNode = Builders.leafBuilder().withNodeIdentifier(
                new NodeIdentifier(ORDINARY_LEAF_B)).withValue("value-b").build();

        final ContainerNode myInnerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_INNER_CONTAINER))
                .withChild(leafListLeafrefNode)
                .withChild(ordinaryLeafANode)
                .withChild(ordinaryLeafBNode).build();

        final Map<QName, Object> keyValues = ImmutableMap.of(KEY_LEAF_A, "value-a", KEY_LEAF_B, "value-b");

        final MapNode myListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(MY_LIST))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(MY_LIST, keyValues))
                        .withChild(referencedLeafListNode).build())
                .build();

        final ContainerNode myContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(MY_CONTAINER))
                .withChild(myListNode)
                .withChild(myInnerContainerNode).build();
        return myContainerNode;
    }
}