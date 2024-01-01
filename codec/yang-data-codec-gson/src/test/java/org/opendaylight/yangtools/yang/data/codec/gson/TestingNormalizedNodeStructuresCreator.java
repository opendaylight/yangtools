/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

final class TestingNormalizedNodeStructuresCreator {
    private static final QNameModule COMPLEX_JSON =
        QNameModule.create(XMLNamespace.of("ns:complex:json"), Revision.of("2014-08-11"));
    private static final QNameModule COMPLEX_JSON_AUG =
        QNameModule.create(XMLNamespace.of("ns:complex:json:augmentation"), Revision.of("2014-08-14"));

    private TestingNormalizedNodeStructuresCreator() {
        // Hidden on purpose
    }

    static ContainerNode cont1Node(final DataContainerChild... children) {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "cont1")))
            .withValue(List.of(children))
            .build();
    }

    static ContainerNode cont2Node() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "cont2")))
            .build();
    }

    private static UnkeyedListNode lst12Node() {
        return ImmutableNodes.newUnkeyedListBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "lst12")))
            .withChild(lst12Entry1Node())
            .build();
    }

    private static UnkeyedListEntryNode lst12Entry1Node() {
        return ImmutableNodes.newUnkeyedListEntryBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "lst12")))
            .withChild(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf121"), "lf121 value"))
            .build();
    }

    private static ChoiceNode choc12Node() {
        return ImmutableNodes.newChoiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "choc12")))
            .withChild(lf17Node())
            .build();
    }

    protected static LeafNode<?> lf17Node() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf17"), "lf17 value");
    }

    private static LeafNode<?> lf15_12NodeExternal() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON_AUG, "lf15_12"), "lf15_12 value from augmentation");
    }

    private static LeafNode<?> lf15_11NodeExternal() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON_AUG, "lf15_11"), "lf15_11 value from augmentation");
    }

    private static ChoiceNode choc11Node(final DataContainerChild... children) {
        return ImmutableNodes.newChoiceBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "choc11")))
            .withValue(List.of(children))
            .build();
    }

    private static LeafNode<?> lf13Node() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf13"), "lf13 value");
    }

    private static LeafNode<?> lf15_21Node() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf15_21"), "lf15_21 value");
    }

    private static LeafNode<?> lf15_12Node() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf15_12"), QName.create(COMPLEX_JSON, "ident"));
    }

    private static LeafNode<Object> lf15_11Node() {
        return ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf15_11"), ImmutableSet.of("one", "two"));
    }

    private static SystemMapNode childLst11() {
        return ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "lst11")))
            .withChild(ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(
                NodeIdentifierWithPredicates.of(QName.create(COMPLEX_JSON, "lst11"), ImmutableMap.of(
                    QName.create(COMPLEX_JSON, "key111"), "key111 value",
                    QName.create(COMPLEX_JSON, "lf111"), "lf111 value")))
                .withChild(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "key111"), "key111 value"))
                .withChild(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf112"), lf112Value()))
                .withChild(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf113"), "lf113 value"))
                .withChild(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf111"), "lf111 value"))
                .build())
            .build();
    }

    private static Object lf112Value() {
        return YangInstanceIdentifier.of(
            new NodeIdentifier(QName.create(COMPLEX_JSON, "cont1")),
            new NodeIdentifier(QName.create(COMPLEX_JSON, "lflst11")),
            new NodeWithValue<>(QName.create(COMPLEX_JSON, "lflst11"), "foo"));
    }

    private static SystemLeafSetNode<?> childLflst11() {
        return ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "lflst11")))
            .withChild(ImmutableNodes.leafSetEntry(QName.create(COMPLEX_JSON, "lflst11"), "lflst11 value1"))
            .withChild(ImmutableNodes.leafSetEntry(QName.create(COMPLEX_JSON, "lflst11"), "lflst11 value2"))
            .build();
    }

    private static SystemLeafSetNode<?> childLflst11Multiline() {
        return ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "lflst11")))
            .withChild(ImmutableNodes.leafSetEntry(QName.create(COMPLEX_JSON, "lflst11"),
                "lflst11 value1\nanother line 1"))
            .withChild(ImmutableNodes.leafSetEntry(QName.create(COMPLEX_JSON, "lflst11"),
                "lflst11 value2\r\nanother line 2"))
            .build();
    }

    public static ContainerNode leafNodeInContainer() {
        return cont1Node(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf11"), 453));
    }

    public static ContainerNode leafListNodeInContainer() {
        return cont1Node(childLflst11());
    }

    public static ContainerNode leafListNodeInContainerMultiline() {
        return cont1Node(childLflst11Multiline());
    }

    public static ContainerNode keyedListNodeInContainer() {
        return cont1Node(childLst11());
    }

    public static ContainerNode leafNodeViaAugmentationInContainer() {
        return cont1Node(ImmutableNodes.leafNode(QName.create(COMPLEX_JSON, "lf12_1"), "lf12 value"));
    }

    public static ContainerNode choiceNodeInContainer() {
        return cont1Node(choc11Node(lf13Node()));
    }

    /**
     * choc11 contains lf13, lf15_11 and lf15_12 are added via external augmentation.
     */
    public static ContainerNode caseNodeAugmentationInChoiceInContainer() {
        return cont1Node(choc11Node(lf15_11Node(), lf15_12Node(), lf13Node(), lf15_21Node()));
    }

    public static ContainerNode caseNodeExternalAugmentationInChoiceInContainer() {
        return cont1Node(choc11Node(lf13Node(), lf15_11Node(), lf15_12Node(), lf15_11NodeExternal(),
            lf15_12NodeExternal()));
    }

    public static ContainerNode choiceNodeAugmentationInContainer() {
        return cont1Node(choc12Node());
    }

    public static ContainerNode unkeyedNodeInContainer() {
        return cont1Node(lst12Node());
    }

    public static ContainerNode topLevelContainer() {
        return cont1Node();
    }

    public static ContainerNode emptyContainerInContainer() {
        return cont1Node(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create(COMPLEX_JSON, "cont11")))
            .build());
    }
}
