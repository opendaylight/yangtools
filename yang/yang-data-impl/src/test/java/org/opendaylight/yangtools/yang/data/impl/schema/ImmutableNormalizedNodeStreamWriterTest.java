/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.dom.DOMSource;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

public class ImmutableNormalizedNodeStreamWriterTest {

    private QNameModule bazModule;

    private QName outerContainer;

    private QName myContainer1;
    private QName myKeyedList;
    private QName myKeyLeaf;
    private QName myLeafInList1;
    private QName myLeafInList2;
    private QName myOrderedList;
    private QName myKeyLeafInOrderedList;
    private QName myLeafInOrderedList1;
    private QName myLeafInOrderedList2;
    private QName myUnkeyedList;
    private QName myLeafInUnkeyedList;
    private QName myLeaf1;
    private QName myLeafList;
    private QName myOrderedLeafList;

    private QName myContainer2;
    private QName innerContainer;
    private QName myLeaf2;
    private QName myLeaf3;
    private QName myChoice;
    private QName myLeafInCase2;
    private QName myAnyxml;

    private QName myContainer3;
    private QName myDoublyKeyedList;
    private QName myFirstKeyLeaf;
    private QName mySecondKeyLeaf;
    private QName myLeafInList3;

    private DOMSource anyxmlDomSource;

    @Before
    public void setup() throws URISyntaxException, ParseException {
        bazModule = QNameModule.create(new URI("baz-namespace"), SimpleDateFormatUtil.getRevisionFormat()
            .parse("1970-01-01"));

        outerContainer = QName.create(bazModule, "outer-container");

        myContainer1 = QName.create(bazModule, "my-container-1");
        myKeyedList = QName.create(bazModule, "my-keyed-list");
        myKeyLeaf = QName.create(bazModule, "my-key-leaf");
        myLeafInList1 = QName.create(bazModule, "my-leaf-in-list-1");
        myLeafInList2 = QName.create(bazModule, "my-leaf-in-list-2");
        myOrderedList = QName.create(bazModule, "my-ordered-list");
        myKeyLeafInOrderedList = QName.create(bazModule, "my-key-leaf-in-ordered-list");
        myLeafInOrderedList1 = QName.create(bazModule, "my-leaf-in-ordered-list-1");
        myLeafInOrderedList2 = QName.create(bazModule, "my-leaf-in-ordered-list-2");
        myUnkeyedList = QName.create(bazModule, "my-unkeyed-list");
        myLeafInUnkeyedList = QName.create(bazModule, "my-leaf-in-unkeyed-list");
        myLeaf1 = QName.create(bazModule, "my-leaf-1");
        myLeafList = QName.create(bazModule, "my-leaf-list");
        myOrderedLeafList = QName.create(bazModule, "my-ordered-leaf-list");

        myContainer2 = QName.create(bazModule, "my-container-2");
        innerContainer = QName.create(bazModule, "inner-container");
        myLeaf2 = QName.create(bazModule, "my-leaf-2");
        myLeaf3 = QName.create(bazModule, "my-leaf-3");
        myChoice = QName.create(bazModule, "my-choice");
        myLeafInCase2 = QName.create(bazModule, "my-leaf-in-case-2");
        myAnyxml = QName.create(bazModule, "my-anyxml");

        myContainer3 = QName.create(bazModule, "my-container-3");
        myDoublyKeyedList = QName.create(bazModule, "my-doubly-keyed-list");
        myFirstKeyLeaf = QName.create(bazModule, "my-first-key-leaf");
        mySecondKeyLeaf = QName.create(bazModule, "my-second-key-leaf");
        myLeafInList3 = QName.create(bazModule, "my-leaf-in-list-3");

        anyxmlDomSource = new DOMSource();
    }

    @Test
    public void testImmutableNormalizedNodeStreamWriter() throws IOException {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter immutableNormalizedNodeStreamWriter = ImmutableNormalizedNodeStreamWriter
                .from(result);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter
                .forStreamWriter(immutableNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(buildOuterContainerNode());

        final NormalizedNode<?, ?> output = result.getResult();
        assertNotNull(output);

        final NormalizedNode<?, ?> expectedNormalizedNode = buildOuterContainerNode();
        assertNotNull(expectedNormalizedNode);

        assertEquals(expectedNormalizedNode, output);
    }

    private NormalizedNode<?, ?> buildOuterContainerNode() {
        // my-container-1
        MapNode myKeyedListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myKeyedList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myKeyedList, myKeyLeaf, "listkeyvalue1"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList1))
                                .withValue("listleafvalue1").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList2))
                                .withValue("listleafvalue2").build()).build())
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myKeyedList, myKeyLeaf, "listkeyvalue2"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList1))
                                .withValue("listleafvalue12").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInList2))
                                .withValue("listleafvalue22").build()).build()).build();

        OrderedMapNode myOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
            new NodeIdentifier(myOrderedList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myOrderedList, myKeyLeafInOrderedList, "olistkeyvalue1"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInOrderedList1))
                                .withValue("olistleafvalue1").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInOrderedList2))
                                .withValue("olistleafvalue2").build()).build())
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myOrderedList, myKeyLeafInOrderedList, "olistkeyvalue2"))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInOrderedList1))
                                .withValue("olistleafvalue12").build())
                        .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInOrderedList2))
                                .withValue("olistleafvalue22").build()).build()).build();

        final UnkeyedListEntryNode unkeyedListEntry = Builders.unkeyedListEntryBuilder()
                .withNodeIdentifier(new NodeIdentifier(myLeafInUnkeyedList))
                .withChild(ImmutableNodes.leafNode(myLeafInUnkeyedList, "foo")).build();

        final List<UnkeyedListEntryNode> unkeyedListEntries = new ArrayList<>();
        unkeyedListEntries.add(unkeyedListEntry);

        UnkeyedListNode myUnkeyedListNode = Builders.unkeyedListBuilder().withNodeIdentifier(
                new NodeIdentifier(myUnkeyedList))
                .withValue(unkeyedListEntries).build();

        LeafNode<?> myLeaf1Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf1))
                .withValue("value1").build();

        LeafSetNode<?> myLeafListNode = Builders.leafSetBuilder().withNodeIdentifier(new NodeIdentifier(myLeafList))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myLeafList, "lflvalue1")).withValue("lflvalue1").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myLeafList, "lflvalue2")).withValue("lflvalue2").build()).build();

        LeafSetNode<?> myOrderedLeafListNode = Builders.orderedLeafSetBuilder().withNodeIdentifier(
                new NodeIdentifier(myOrderedLeafList))
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myOrderedLeafList, "olflvalue1")).withValue("olflvalue1").build())
                .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(
                        new NodeWithValue<>(myOrderedLeafList, "olflvalue2")).withValue("olflvalue2").build()).build();

        ContainerNode myContainer1Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer1))
                .withChild(myKeyedListNode)
                .withChild(myOrderedListNode)
                .withChild(myUnkeyedListNode)
                .withChild(myLeaf1Node)
                .withChild(myLeafListNode)
                .withChild(myOrderedLeafListNode)
                .build();

        // my-container-2
        ContainerNode innerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(innerContainer))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf2))
                        .withValue("value2").build()).build();

        LeafNode<?> myLeaf3Node = Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeaf3))
                .withValue("value3").build();

        ChoiceNode myChoiceNode = Builders.choiceBuilder().withNodeIdentifier(new NodeIdentifier(myChoice))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(myLeafInCase2))
                        .withValue("case2value").build()).build();

        AnyXmlNode myAnyxmlNode = Builders.anyXmlBuilder().withNodeIdentifier(new NodeIdentifier(myAnyxml))
                .withValue(anyxmlDomSource).build();

        ContainerNode myContainer2Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer2))
                .withChild(innerContainerNode)
                .withChild(myLeaf3Node)
                .withChild(myChoiceNode)
                .withChild(myAnyxmlNode).build();

        // my-container-3
        Map<QName, Object> keys = new HashMap<>();
        keys.put(myFirstKeyLeaf, "listkeyvalue1");
        keys.put(mySecondKeyLeaf, "listkeyvalue2");

        MapNode myDoublyKeyedListNode = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(myDoublyKeyedList))
                .withChild(Builders.mapEntryBuilder().withNodeIdentifier(
                        new NodeIdentifierWithPredicates(myDoublyKeyedList, keys))
                        .withChild(Builders.leafBuilder().withNodeIdentifier(
                                new NodeIdentifier(myLeafInList3)).withValue("listleafvalue1").build()).build())
                .build();

        AugmentationNode myDoublyKeyedListAugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Sets.newHashSet(myDoublyKeyedList)))
                .withChild(myDoublyKeyedListNode).build();

        ContainerNode myContainer3Node = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(myContainer3))
                .withChild(myDoublyKeyedListAugNode).build();

        AugmentationNode myContainer3AugNode = Builders.augmentationBuilder().withNodeIdentifier(
                new AugmentationIdentifier(Sets.newHashSet(myContainer3)))
                .withChild(myContainer3Node).build();

        ContainerNode outerContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(outerContainer))
                .withChild(myContainer1Node)
                .withChild(myContainer2Node)
                .withChild(myContainer3AugNode).build();

        return outerContainerNode;
    }
}
