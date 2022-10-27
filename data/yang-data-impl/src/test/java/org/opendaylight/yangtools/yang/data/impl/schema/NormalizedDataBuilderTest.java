/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;

public class NormalizedDataBuilderTest {
    @Test
    public void testSchemaUnaware() {
        // Container
        DataContainerNodeBuilder<NodeIdentifier, ContainerNode> builder = Builders
                .containerBuilder().withNodeIdentifier(getNodeIdentifier("container"));

        // leaf
        LeafNode<String> leafChild = Builders.<String>leafBuilder().withNodeIdentifier(getNodeIdentifier("leaf"))
                .withValue("String").build();
        builder.withChild(leafChild);

        // leafList
        LeafSetNode<Integer> leafList = Builders.<Integer>leafSetBuilder()
                .withNodeIdentifier(getNodeIdentifier("leaf"))
                .withChildValue(1)
                .withChild(Builders.<Integer>leafSetEntryBuilder()
                        .withNodeIdentifier(getNodeWithValueIdentifier("leaf", 3)).withValue(3).build())
                .build();
        builder.withChild(leafList);

        // list
        MapEntryNode listChild1 = Builders
                .mapEntryBuilder()
                .withChild(
                        Builders.<Integer>leafBuilder().withNodeIdentifier(getNodeIdentifier("uint32InList"))
                                .withValue(1).build())
                .withChild(Builders.containerBuilder().withNodeIdentifier(getNodeIdentifier("containerInList")).build())
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                                getNodeIdentifier("list").getNodeType(), Collections.singletonMap(
                                getNodeIdentifier("uint32InList").getNodeType(), 1))).build();

        builder
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(getNodeIdentifier("list"))
                .withChild(listChild1)
                .build())
            .withChild(Builders.<Integer>leafBuilder()
                .withNodeIdentifier(getNodeIdentifier("augmentUint32"))
                .withValue(11).build());

        // This works without schema (adding child from augment as a direct
        // child)
        builder.withChild(Builders.<Integer>leafBuilder().withNodeIdentifier(getNodeIdentifier("augmentUint32"))
                .withValue(11).build());
    }

    private static <T> NodeWithValue<T> getNodeWithValueIdentifier(final String localName, final T value) {
        return new NodeWithValue<>(getQName(localName), value);
    }

    private static QName getQName(final String localName) {
        return QName.create(XMLNamespace.of("namespace"), localName);
    }

    private static NodeIdentifier getNodeIdentifier(final String localName) {
        return new NodeIdentifier(getQName(localName));
    }
}
